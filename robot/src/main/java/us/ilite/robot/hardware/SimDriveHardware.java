package us.ilite.robot.hardware;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import com.team254.lib.geometry.Rotation2d;

import us.ilite.common.lib.RobotProfile;
import com.team254.frc2018.Kinematics;
import us.ilite.common.lib.odometry.RobotStateEstimator;
import us.ilite.common.lib.util.Conversions;
import us.ilite.lib.drivers.Clock;
import us.ilite.robot.modules.DriveMessage;

/**
 * Simple drivebase simulation. Only supports velocity control (for now).
 * Hopefully we'll be able to replace this with SnobotSim later in the season.
 */
public class SimDriveHardware implements IDriveHardware {

    private final ILog mLogger = Logger.createLog(DriveHardware.class);

    private RobotStateEstimator mEncoderStateEstimator;
    
    private DriveMessage mDriveMessage;
    public SimTalonEncoder mLeftEncoder = new SimTalonEncoder();
    public SimTalonEncoder mRightEncoder = new SimTalonEncoder();

    private Clock mClock;
    private double mLastTime = 0.0;

    public SimDriveHardware(Clock pClock, RobotProfile pRobotProfile) {
        mClock = pClock;
        mEncoderStateEstimator = new RobotStateEstimator(new Kinematics(pRobotProfile));
    }

    @Override
    public void init() {
        zero();
    }

    @Override
    public void zero() {
        mEncoderStateEstimator.reset();

        mLeftEncoder.zero();
        mRightEncoder.zero();
    }

    public void set(DriveMessage pDriveMessage) {
        mDriveMessage = pDriveMessage;
        update(mClock.getCurrentTime());
    }

    public void configureMode(ControlMode pControlMode) {
        
    }
    
    public void update(double pNow) {
        double dt = pNow - mLastTime;
        mLogger.debug("Updating");
        if(mDriveMessage.leftControlMode == ControlMode.Velocity && mDriveMessage.rightControlMode == ControlMode.Velocity) {
            mLeftEncoder.update(dt, mDriveMessage.leftOutput);
            mRightEncoder.update(dt, mDriveMessage.rightOutput);
            mLogger.debug(String.format(
                "\nCommanded Vel: (%s, %s)\nVel: (%s, %s)\nPos Ticks: (%s, %s)\nPos Inches: (%s, %s)\n", 
                mDriveMessage.leftOutput,
                mDriveMessage.rightOutput,
                mLeftEncoder.getVelocity(),
                mRightEncoder.getVelocity(),
                mLeftEncoder.getPosition(),
                mRightEncoder.getPosition(),
                getLeftInches(),
                getRightInches()

            ));
        } else {
            mLogger.error("Control mode ", mDriveMessage.leftControlMode, " and ", mDriveMessage.rightControlMode, " is not supported in simulation.");
        }

        mEncoderStateEstimator.update(pNow, getLeftInches(), getRightInches());
        System.out.println(mEncoderStateEstimator.getRobotState().getLatestFieldToVehiclePose().getRotation());

        mLastTime = pNow;
    }
    
    public Rotation2d getHeading() {
        return mEncoderStateEstimator.getRobotState().getLatestFieldToVehiclePose().getRotation();
    }

    public double getLeftInches() {
        return Conversions.ticksToInches((int)mLeftEncoder.getPosition());
    }

    public double getRightInches() {
        return Conversions.ticksToInches((int)mRightEncoder.getPosition());
    }

    public int getLeftVelTicks() {
        return (int)mLeftEncoder.getVelocity();
    }

    public int getRightVelTicks() {
        return (int)mRightEncoder.getVelocity();
    }

    public double getLeftVelInches() {
        return Conversions.ticksPer100msToInchesPerSecond((int)mLeftEncoder.getVelocity());
    }

    public double getRightVelInches() {
        return Conversions.ticksPer100msToInchesPerSecond((int)mRightEncoder.getVelocity());
    }

    //TODO
    @Override
    public double getLeftCurrent() {
        return 0;
    }

    //TODO
    @Override
    public double getRightCurrent() {
        return 0;
    }

    //TODO
    @Override
    public double getLeftVoltage() {
        return 0;
    }

    //TODO
    @Override
    public double getRightVoltage() {
        return 0;
    }

    @Override
    public boolean checkHardware() {
        return false;
    }

}
