package odometry;

import lib.geometry.Pose2d;
import lib.geometry.Rotation2d;
import lib.geometry.Twist2d;

public class RobotStateEstimator {

    private RobotState mRobotState;
    private Kinematics mKinematicModel;
    private double mLastLeftPosition, mLastRightPosition, mLastTimestamp;


    public RobotStateEstimator(Kinematics pKinematicModel) {
        mKinematicModel = pKinematicModel;
        mRobotState = new RobotState(mKinematicModel);
        reset();
    }

    public void update(double pTimestamp, double pLeftPosition, double pRightPosition) {

        double dt = pTimestamp - mLastTimestamp;

        Twist2d delta = mRobotState.generateOdometryFromSensors(pLeftPosition - mLastLeftPosition, pRightPosition - mLastRightPosition);
        Twist2d velocity = new Twist2d(delta.dx / dt, delta.dy / dt, delta.dtheta / dt);

        mRobotState.addObservations(pTimestamp, delta, velocity);

        mLastTimestamp = pTimestamp;
        mLastLeftPosition = pLeftPosition;
        mLastRightPosition = pRightPosition;

    }

    public void update(double pTimestamp, double pLeftPosition, double pRightPosition, Rotation2d pCurrentHeading) {

        double dt = pTimestamp - mLastTimestamp;

        Twist2d delta = mRobotState.generateOdometryFromSensors(pLeftPosition - mLastLeftPosition, pRightPosition - mLastRightPosition,
                pCurrentHeading);
        Twist2d velocity = new Twist2d(delta.dx / dt, delta.dy / dt, delta.dtheta / dt);

        mRobotState.addObservations(pTimestamp, delta, velocity);

        mLastTimestamp = pTimestamp;
        mLastLeftPosition = pLeftPosition;
        mLastRightPosition = pRightPosition;

    }

    public void update(double pTimestamp, double pLeftPosition, double pRightPosition, double pLeftVelocity, double pRightVelocity, Rotation2d pCurrentHeading) {


        Twist2d delta = mRobotState.generateOdometryFromSensors(pLeftPosition - mLastLeftPosition, pRightPosition - mLastRightPosition,
                pCurrentHeading);
        Twist2d velocity = mKinematicModel.forwardKinematics(pLeftVelocity, pRightVelocity);

        mRobotState.addObservations(pTimestamp, delta, velocity);

        mLastTimestamp = pTimestamp;
        mLastLeftPosition = pLeftPosition;
        mLastRightPosition = pRightPosition;
    }

    public void reset() {
        reset(0.0, new Pose2d());
    }

    public void reset(double time, Pose2d field_to_vehicle) {
        mRobotState.reset(time, field_to_vehicle);
        mLastTimestamp = 0.0;
        mLastLeftPosition = mLastRightPosition = 0.0;
    }

    public RobotState getRobotState() {
        return mRobotState;
    }

}
