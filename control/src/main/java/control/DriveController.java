package control;

import us.ilite.common.lib.geometry.Pose2d;
import us.ilite.common.lib.geometry.Pose2dWithCurvature;
import us.ilite.common.lib.geometry.Rotation2d;
import us.ilite.common.lib.physics.DCMotorTransmission;
import us.ilite.common.lib.physics.DifferentialDrive;
import us.ilite.common.lib.trajectory.TimedView;
import us.ilite.common.lib.trajectory.Trajectory;
import us.ilite.common.lib.trajectory.TrajectoryIterator;
import us.ilite.common.lib.trajectory.timing.TimedState;
import odometry.Kinematics;
import odometry.RobotStateEstimator;
import profiles.RobotProfile;

/**
 * High level manager for pose tracking, path/trajectory following, and pose stabilization.
 */
public class DriveController {

    private final double kDt;

    private final RobotProfile mRobotProfile;
    private final DCMotorTransmission mLeftTransmission, mRightTransmission;
    private final DifferentialDrive mDriveModel;
    private final Kinematics mKinematicModel;

    private final NonlinearFeedbackController mController;
    private final DriveMotionPlanner mDriveMotionPlanner;
    private final RobotStateEstimator mRobotStateEstimator;

    public DriveController(RobotProfile pRobotProfile, double pDt) {
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

        kDt = pDt;

    }

    public DriveOutput getOutput(double pTimestamp, double pLeftAbsolutePos, double pRightAbsolutePos, Rotation2d pHeading) {
        mRobotStateEstimator.update(pTimestamp, pLeftAbsolutePos, pRightAbsolutePos, pHeading);

        return mDriveMotionPlanner.update(pTimestamp, mRobotStateEstimator.getRobotState().getLatestFieldToVehiclePose());
    }

    public DriveOutput getOutput(double pTimestamp, double pLeftAbsolutePos, double pRightAbsolutePos) {
        mRobotStateEstimator.update(pTimestamp, pLeftAbsolutePos, pRightAbsolutePos);

        return mDriveMotionPlanner.update(pTimestamp, mRobotStateEstimator.getRobotState().getLatestFieldToVehiclePose());
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
}
