package com.team254.frc2018.planners;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import com.team254.lib.geometry.Pose2d;
import com.team254.lib.geometry.Pose2dWithCurvature;
import com.team254.lib.geometry.Rotation2d;
import com.team254.lib.physics.DifferentialDrive;
import com.team254.lib.trajectory.*;
import com.team254.lib.trajectory.timing.TimedState;
import com.team254.lib.util.CSVWritable;
import com.team254.lib.util.Units;
import us.ilite.common.lib.control.AController;
import com.team254.lib.physics.DriveOutput;
import us.ilite.common.lib.trajectory.TrajectoryGenerator;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * DriveMotionPlanner is responsible for generating both trajectories and the feedforward commands needed to follow them.
 * Trajectory followers are entirely modular and can be switched at-will.
 * Port from Team 254's 2018 robot code (https://github.com/Team254/FRC-2018-Public) modified to support a generic 
 * set of robot parameters, generic motion controllers, and additional planning modes. Trajectory generation has
 * been moved out of this class and into TrajectoryGenerator
 */
public class DriveMotionPlanner implements CSVWritable {

    private ILog mLog = Logger.createLog(DriveMotionPlanner.class);

    private final DifferentialDrive mDriveModel;

    private AController mController;
    private PlannerMode mPlannerMode;
    
    public DriveMotionPlanner(DifferentialDrive pDriveModel, PlannerMode pPlannerMode, AController pController) {
        mDriveModel = pDriveModel;
        mController = pController;
        mPlannerMode = pPlannerMode;
    }

    public enum PlannerMode {
        FEEDFORWARD_ONLY,
        FEEDBACK,
        FEEDBACK_NO_DYNAMICS,
        FEEDFORWARD_NO_DYNAMICS
    }

    // Trajectory and errors are in inches
    TrajectoryIterator<TimedState<Pose2dWithCurvature>> mCurrentTrajectory = null;
    public TimedState<Pose2dWithCurvature> mSetpoint = new TimedState<>(Pose2dWithCurvature.identity());
    Pose2d mError = Pose2d.identity();
    boolean mIsReversed = false;
    boolean mIsTurnInPlace = false;

    // Rad / s. Taken from previous dynamics output (for now)
    DifferentialDrive.ChassisState prev_velocity_ = new DifferentialDrive.ChassisState();
    double mLastTime = Double.POSITIVE_INFINITY;
    double mDt = 0.0;

    DriveOutput mOutput = new DriveOutput();

    public void reset() {
        mError = Pose2d.identity();
        mOutput = new DriveOutput();
        mLastTime = Double.POSITIVE_INFINITY;
    }

    public void setTrajectory(final TrajectoryIterator<TimedState<Pose2dWithCurvature>> trajectory) {
        mCurrentTrajectory = trajectory;
        mSetpoint = trajectory.getState();
        mIsReversed = TrajectoryGenerator.isReversed(trajectory);
        mIsTurnInPlace = false;
    }

    public void setRotationTrajectory(final TrajectoryIterator<TimedState<Rotation2d>> trajectory) {
        List<TimedState<Pose2dWithCurvature>> timedRotationPoses = new ArrayList<>();

        for(int i = 0; i < trajectory.trajectory().length(); i++) {
            TimedState<Rotation2d> timedRotationState = trajectory.trajectory().getState(i);
            TimedState<Pose2dWithCurvature> timedRotationPoseState = new TimedState<>(new Pose2dWithCurvature(Pose2d.fromRotation(timedRotationState.state()), Double.NaN),
                                                                                      timedRotationState.t(),
                                                                                      timedRotationState.velocity(),
                                                                                      timedRotationState.acceleration());
            timedRotationPoses.add(timedRotationPoseState);
        }

        TrajectoryIterator<TimedState<Pose2dWithCurvature>> trajectoryIterator = new TrajectoryIterator<>(new TimedView<>(new Trajectory<>(timedRotationPoses)));

        mCurrentTrajectory = trajectoryIterator;
        mSetpoint = trajectoryIterator.getState();
        mIsTurnInPlace = true;
    }

    @Override
    public String toCSV() {
        DecimalFormat fmt = new DecimalFormat("#0.000");
        return fmt.format(mOutput.left_velocity) + "," + fmt.format(mOutput.right_velocity) + "," +
                fmt.format(mOutput.left_accel) + ", " + fmt.format(mOutput.right_accel) +
                fmt.format(mOutput.left_feedforward_voltage) + "," + fmt.format(mOutput.right_feedforward_voltage) + "," +
                mSetpoint.toCSV();
    }

    /**
     *
     * @param timestamp The current timestamp, in seconds
     * @param current_state The current (x, y, theta) pose of the robot
     * @return A DriveOutput object containing velocity, acceleration, and voltage
     */
    public DriveOutput update(double timestamp, Pose2d current_state) {
        // No trajectory? Do nothing!
        if (mCurrentTrajectory == null) return new DriveOutput();

        // If DriveMotionPlanner has been reset and we haven't started the trajectory, set it to our timestamp.
        if (mCurrentTrajectory.getProgress() == 0.0 && !Double.isFinite(mLastTime)) {
            mLog.info("Starting trajectory with timestamp: ", timestamp);
            mLastTime = timestamp;
        }

        mDt = timestamp - mLastTime;
        // Advance the setpoint by the amount of time that has passed between this update and the last update.
        TrajectorySamplePoint<TimedState<Pose2dWithCurvature>> sample_point = mCurrentTrajectory.advance(mDt);

        if (!mCurrentTrajectory.isDone()) {

            mSetpoint = sample_point.state();
            mError = current_state.inverse().transformBy(mSetpoint.state().getPose());

            DifferentialDrive.DriveDynamics dynamics;

            if(mIsTurnInPlace) {

                dynamics = mDriveModel.solveInverseDynamics(new DifferentialDrive.ChassisState(0.0, mSetpoint.velocity()),
                                                            new DifferentialDrive.ChassisState(0.0, mSetpoint.acceleration()));
                // Modify our setpoint to our current (x,y) position. This way we can use the controller without having it compensate for cross-track error.
                mSetpoint = new TimedState<>(new Pose2dWithCurvature(current_state.getTranslation(),
                                                                                        mSetpoint.state().getRotation(),
                                                                                        Double.POSITIVE_INFINITY),
                                                                                        mSetpoint.t(), mSetpoint.velocity(), mSetpoint.acceleration());

//                mOutput = mController.update(mCurrentTrajectory, mSetpoint, dynamics, prev_velocity_, current_state, mDt);
                mOutput = new DriveOutput(dynamics.wheel_velocity.left, dynamics.wheel_velocity.right, dynamics
                        .wheel_acceleration.left, dynamics.wheel_acceleration.right, dynamics.voltage
                        .left, dynamics.voltage.right);

            } else {
                // Generate feedforward voltages and convert everything to SI.
                final double velocity_m = Units.inches_to_meters(mSetpoint.velocity());
                final double curvature_m = Units.meters_to_inches(mSetpoint.state().getCurvature());
                final double dcurvature_ds_m = Units.meters_to_inches(Units.meters_to_inches(mSetpoint.state()
                        .getDCurvatureDs()));
                final double acceleration_m = Units.inches_to_meters(mSetpoint.acceleration());

                // To obtain angular acceleration, we use theta = d / r, then use the 2nd derivative of curvature to calculate additional acceleration.
                dynamics = mDriveModel.solveInverseDynamics(
                        new DifferentialDrive.ChassisState(velocity_m, velocity_m * curvature_m),
                        new DifferentialDrive.ChassisState(acceleration_m,
                                acceleration_m * curvature_m + velocity_m * velocity_m * dcurvature_ds_m));

                switch(mPlannerMode) {
                    case FEEDFORWARD_ONLY:
                        mOutput = new DriveOutput(dynamics.wheel_velocity.left, dynamics.wheel_velocity.right, dynamics
                                .wheel_acceleration.left, dynamics.wheel_acceleration.right, dynamics.voltage
                                .left, dynamics.voltage.right);
                        break;
                    case FEEDBACK:
                        mOutput = mController.update(mCurrentTrajectory, mSetpoint, dynamics, prev_velocity_, current_state, mDt);
                        break;
                    case FEEDBACK_NO_DYNAMICS:
                        mOutput = mController.update(mCurrentTrajectory, mSetpoint, dynamics, prev_velocity_, current_state, mDt);
                        // Only account for friction
                        mOutput.left_feedforward_voltage = mDriveModel.left_transmission().friction_voltage();
                        mOutput.right_feedforward_voltage = mDriveModel.right_transmission().friction_voltage();
                        break;
                    case FEEDFORWARD_NO_DYNAMICS:
                        mOutput = new DriveOutput(dynamics.wheel_velocity.left, dynamics.wheel_velocity.right, dynamics
                                .wheel_acceleration.left, dynamics.wheel_acceleration.right, dynamics.voltage
                                .left, dynamics.voltage.right);
                        // Only account for friction
                        mOutput.left_feedforward_voltage = mDriveModel.left_transmission().friction_voltage();
                        mOutput.right_feedforward_voltage = mDriveModel.right_transmission().friction_voltage();
                        break;
                }
            }

            prev_velocity_ = dynamics.chassis_velocity;

        } else {
            // TODO Possibly switch to a pose stabilizing controller?
            mOutput = new DriveOutput();
        }

        mLastTime = timestamp;

        return mOutput;
    }

    public boolean isDone() {
        return mCurrentTrajectory != null && mCurrentTrajectory.isDone();
//        return (mError.getTranslation().x() < 3.0 && mError.getTranslation().y() < 3.0);
    }

    public Pose2d error() {
        return mError;
    }

    public TimedState<Pose2dWithCurvature> setpoint() {
        return mSetpoint;
    }
    public DifferentialDrive getDriveModel() {
        return mDriveModel;
    }

    public PlannerMode getPlannerMode() {
        return mPlannerMode;
    }

    public void setPlannerMode(PlannerMode pPlannerMode) {
        mPlannerMode = pPlannerMode;
    }

}
