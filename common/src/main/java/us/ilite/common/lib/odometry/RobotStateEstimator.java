package us.ilite.common.lib.odometry;

import com.team254.frc2018.Kinematics;
import com.team254.frc2018.RobotState;
import com.team254.lib.geometry.Pose2d;
import com.team254.lib.geometry.Rotation2d;
import com.team254.lib.geometry.Twist2d;


public class RobotStateEstimator {

    private RobotState mRobotState;
    private Kinematics mKinematicModel;
    private double mLastLeftPosition, mLastRightPosition, mLastTimestamp;


    public RobotStateEstimator(Kinematics pKinematicModel) {
        mKinematicModel = pKinematicModel;
        mRobotState = new RobotState(mKinematicModel);
        reset();
    }

    public Pose2d update(double pTimestamp, double pLeftPosition, double pRightPosition) {

        double dt = pTimestamp - mLastTimestamp;

        Twist2d delta = mRobotState.generateOdometryFromSensors(pLeftPosition - mLastLeftPosition, pRightPosition - mLastRightPosition);
        Twist2d velocity = new Twist2d(delta.dx / dt, delta.dy / dt, delta.dtheta / dt);

        Pose2d currentPose = mRobotState.addObservations(pTimestamp, delta, velocity);

        mLastTimestamp = pTimestamp;
        mLastLeftPosition = pLeftPosition;
        mLastRightPosition = pRightPosition;

        return currentPose;
    }

    public Pose2d update(double pTimestamp, double pLeftPosition, double pRightPosition, Rotation2d pCurrentHeading) {

        double dt = pTimestamp - mLastTimestamp;

        Twist2d delta = mRobotState.generateOdometryFromSensors(pLeftPosition - mLastLeftPosition, pRightPosition - mLastRightPosition,
                pCurrentHeading);
        Twist2d velocity = new Twist2d(delta.dx / dt, delta.dy / dt, delta.dtheta / dt);

        Pose2d currentPose = mRobotState.addObservations(pTimestamp, delta, velocity);

        mLastTimestamp = pTimestamp;
        mLastLeftPosition = pLeftPosition;
        mLastRightPosition = pRightPosition;

        return currentPose;
    }

    public Pose2d update(double pTimestamp, double pLeftPosition, double pRightPosition, double pLeftVelocity, double pRightVelocity, Rotation2d pCurrentHeading) {


        Twist2d delta = mRobotState.generateOdometryFromSensors(pLeftPosition - mLastLeftPosition, pRightPosition - mLastRightPosition,
                pCurrentHeading);
        Twist2d velocity = mKinematicModel.forwardKinematics(pLeftVelocity, pRightVelocity);

        Pose2d currentPose = mRobotState.addObservations(pTimestamp, delta, velocity);

        mLastTimestamp = pTimestamp;
        mLastLeftPosition = pLeftPosition;
        mLastRightPosition = pRightPosition;

        return currentPose;
    }

    public final void reset() {
        reset(0.0, new Pose2d());
    }

    public final void reset(double time, Pose2d field_to_vehicle) {
        mRobotState.reset(time, field_to_vehicle);
        mLastTimestamp = 0.0;
        mLastLeftPosition = mLastRightPosition = 0.0;
    }

    public RobotState getRobotState() {
        return mRobotState;
    }

}
