package us.ilite.common.lib.control;

import com.team254.frc2018.planners.DriveMotionPlanner;
import com.team254.frc2018.planners.NonlinearFeedbackController;
import com.team254.lib.geometry.Pose2d;
import com.team254.lib.geometry.Pose2dWithCurvature;
import com.team254.lib.geometry.Rotation2d;
import com.team254.lib.physics.DCMotorTransmission;
import com.team254.lib.physics.DifferentialDrive;
import com.team254.lib.physics.DriveOutput;
import com.team254.lib.trajectory.TimedView;
import com.team254.lib.trajectory.Trajectory;
import com.team254.lib.trajectory.TrajectoryIterator;
import com.team254.lib.trajectory.TrajectoryUtil;
import com.team254.lib.trajectory.timing.TimedState;
import com.team254.frc2018.Kinematics;
import us.ilite.common.lib.odometry.RobotStateEstimator;
import us.ilite.common.lib.RobotProfile;
import us.ilite.common.lib.util.PerfTimer;

/**
 * High level manager for pose tracking, path/trajectory following, and pose stabilization.
 */
public class DriveController {

    private final RobotProfile mRobotProfile;
    private final DCMotorTransmission mLeftTransmission, mRightTransmission;
    private final DifferentialDrive mDriveModel;
    private final Kinematics mKinematicModel;

    private final NonlinearFeedbackController mController;
    private final DriveMotionPlanner mDriveMotionPlanner;
    private final RobotStateEstimator mRobotStateEstimator;

    private Pose2d mCurrentPose = new Pose2d();

//    private PerfTimer mStateEstimatorTimer = new PerfTimer().alwayLog().setLogMessage("State Estimation: %s");
//    private PerfTimer mMotionPlannerTimer = new PerfTimer().alwayLog().setLogMessage("Motion Plan Update: %s");

    public DriveController(RobotProfile pRobotProfile) {
        mRobotProfile = pRobotProfile;
        // Invert our feedforward constants. Torque constant is kT = I * kA, where I is the robot modeled as a cylindrical load on the transmission and kA is the inverted feedforward.
        mLeftTransmission = new DCMotorTransmission(1 / mRobotProfile.getLeftVoltPerSpeed(), mRobotProfile.getCylindricalMoi() / mRobotProfile.getLeftVoltPerAccel(), mRobotProfile.getLeftFrictionVoltage());
        mRightTransmission = new DCMotorTransmission(1 / mRobotProfile.getRightVoltPerSpeed(), mRobotProfile.getCylindricalMoi() / mRobotProfile.getRightVoltPerAccel(), mRobotProfile.getRightFrictionVoltage());
        mDriveModel = new DifferentialDrive(mRobotProfile.getLinearInertia(), mRobotProfile.getAngularInertia(), mRobotProfile.getAngularDrag(), mRobotProfile.getWheelRadiusMeters(), mRobotProfile.getWheelbaseRadiusMeters(),
                mLeftTransmission, mRightTransmission);
        mKinematicModel = new Kinematics(mRobotProfile);

        mController = new NonlinearFeedbackController(mDriveModel, 0.65, 0.175);
        mDriveMotionPlanner = new DriveMotionPlanner(mDriveModel, DriveMotionPlanner.PlannerMode.FEEDFORWARD_ONLY, mController);
        mRobotStateEstimator = new RobotStateEstimator(mKinematicModel);

    }

    public DriveOutput update(double pTimestamp, double pLeftAbsolutePos, double pRightAbsolutePos, Rotation2d pHeading) {
//        mStateEstimatorTimer.start();
        mCurrentPose = mRobotStateEstimator.update(pTimestamp, pLeftAbsolutePos, pRightAbsolutePos, pHeading);
//        mStateEstimatorTimer.stop();

//        mMotionPlannerTimer.start();
        DriveOutput output = mDriveMotionPlanner.update(pTimestamp, mCurrentPose);
//        mMotionPlannerTimer.stop();

        return output;
    }

    public DriveOutput update(double pTimestamp, double pLeftAbsolutePos, double pRightAbsolutePos) {
        mCurrentPose = mRobotStateEstimator.update(pTimestamp, pLeftAbsolutePos, pRightAbsolutePos);

        return mDriveMotionPlanner.update(pTimestamp, mCurrentPose);
    }

    public DriveController setPlannerMode(DriveMotionPlanner.PlannerMode pPlannerMode) {
        mDriveMotionPlanner.setPlannerMode(pPlannerMode);
        return this;
    }

    public DriveController setPose(Pose2d pPose) {
        mRobotStateEstimator.reset(0.0, pPose);
        return this;
    }

    public DriveController setTrajectory(Trajectory<TimedState<Pose2dWithCurvature>> pTrajectory, boolean pResetToTrajectoryStart) {
        TrajectoryIterator<TimedState<Pose2dWithCurvature>> iterator = new TrajectoryIterator<>(new TimedView<>(pTrajectory));
        mDriveMotionPlanner.reset();
        mDriveMotionPlanner.setTrajectory(iterator);

        if(pResetToTrajectoryStart) {
            mRobotStateEstimator.reset(pTrajectory.getFirstState().t(), pTrajectory.getFirstState().state().getPose());
        }

        return this;
    }

    public DriveController setRotationTrajectory(Trajectory<TimedState<Rotation2d>> pTrajectory) {
        TrajectoryIterator<TimedState<Rotation2d>> iterator = new TrajectoryIterator<>(new TimedView<>(pTrajectory));
        mDriveMotionPlanner.setRotationTrajectory(iterator);

        return this;
    }

    public boolean isDone() {
        return mDriveMotionPlanner.isDone();
    }

    public DriveMotionPlanner getDriveMotionPlanner() {
        return mDriveMotionPlanner;
    }

    public RobotStateEstimator getRobotStateEstimator() {
        return mRobotStateEstimator;
    }

    public NonlinearFeedbackController getController() {
        return mController;
    }

    public RobotProfile getRobotProfile() {
        return mRobotProfile;
    }

    public DCMotorTransmission getLeftTransmission() {
        return mLeftTransmission;
    }

    public DCMotorTransmission getRightTransmission() {
        return mRightTransmission;
    }

    public Pose2d getCurrentPose() {
        return mCurrentPose;
    }
}
