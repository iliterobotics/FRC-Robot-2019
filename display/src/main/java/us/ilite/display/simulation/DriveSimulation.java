package us.ilite.display.simulation;

import java.util.ArrayList;
import java.util.List;

import com.team254.lib.geometry.Pose2d;
import com.team254.lib.geometry.Pose2dWithCurvature;
import com.team254.lib.geometry.Rotation2d;
import com.team254.lib.physics.DifferentialDrive.WheelState;
import com.team254.lib.trajectory.Trajectory;
import com.team254.lib.trajectory.timing.TimedState;
import com.team254.lib.util.ReflectingCSVWriter;
import com.team254.lib.util.Units;
import com.team254.lib.util.Util;

import us.ilite.common.lib.control.DriveController;
import us.ilite.common.lib.control.DriveMotionPlanner;
import us.ilite.common.lib.control.DriveOutput;

public class DriveSimulation {
    
    ReflectingCSVWriter<Pose2d> mOdometryWriter;
    ReflectingCSVWriter<DriveMotionPlanner> mTrajectoryWriter;
    
    DriveController mDriveController;
    WheelState mWheelDisplacement = new WheelState();
    private final double kDt;
    double time = 0.0;

    List<ISimulationListener> mSimulationListeners = new ArrayList<>();

    public DriveSimulation(DriveController pDriveController, ReflectingCSVWriter<Pose2d> pOdometryWriter, ReflectingCSVWriter<DriveMotionPlanner> pTrajectoryWriter, double pDt) {
        mDriveController = pDriveController;
        mOdometryWriter = pOdometryWriter;
        mTrajectoryWriter = pTrajectoryWriter;
        kDt = pDt;
    }

    public double driveTrajectory(Trajectory<TimedState<Rotation2d>> pTrajectoryToDrive) {
        double startTime = time;

        mDriveController.setRotationTrajectory(pTrajectoryToDrive);

        simulate();

        System.out.println("Trajectory time: " + (time - startTime));

        return time - startTime;
    }

    public double driveTrajectory(Trajectory<TimedState<Pose2dWithCurvature>> pTrajectoryToDrive, boolean pResetPoseToTrajectoryStart) {
        double startTime = time;

        mDriveController.setTrajectory(pTrajectoryToDrive, pResetPoseToTrajectoryStart);

        simulate();

        System.out.println("Trajectory time: " + (time - startTime));

        return time - startTime;
    }

    private void simulate() {
        for (; !mDriveController.isDone(); time += kDt) {

            Pose2d currentPose = mDriveController.getRobotStateEstimator().getRobotState().getLatestFieldToVehiclePose();
            DriveOutput output = mDriveController.update(time, mWheelDisplacement.left, mWheelDisplacement.right);

            mTrajectoryWriter.add(mDriveController.getDriveMotionPlanner());
            mOdometryWriter.add(currentPose);

            mTrajectoryWriter.flush();
            mOdometryWriter.flush();

            mSimulationListeners.forEach(l -> l.update(time, currentPose));

            if(Math.abs(output.left_feedforward_voltage) > 12.0 || Math.abs(output.right_feedforward_voltage) > 12.0) {
                System.err.println("Warning: Output above 12.0 volts.");
                // Limit velocity
                output.left_velocity = Util.limit(output.left_velocity, 120.0);
                output.right_velocity = Util.limit(output.right_velocity, 120.0);
            }

            // Our pose estimator expects input in inches, not radians. We happily oblige.
            output = output.rads_to_inches(Units.meters_to_inches(mDriveController.getRobotProfile().getWheelRadiusMeters()));

            // Update the total distance each wheel has traveled, in inches.
            mWheelDisplacement = new WheelState(mWheelDisplacement.left + (output.left_velocity * kDt) + (0.5 * output.left_accel * kDt * kDt),
                    mWheelDisplacement.right + (output.right_velocity * kDt) + (0.5 * output.right_accel * kDt * kDt));

        }
    }

    public void addListener(ISimulationListener pListener) {
        mSimulationListeners.add(pListener);
    }

    public void setPose(Pose2d pRobotPose) {
        mDriveController.getRobotStateEstimator().reset(0.0, pRobotPose);
    }

}
