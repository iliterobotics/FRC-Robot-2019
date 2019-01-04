package us.ilite.robot.hardware;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.DemandType;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import control.DriveController;
import us.ilite.common.config.SystemSettings;
import com.team254.lib.geometry.Rotation2d;
import com.team254.lib.physics.DCMotorTransmission;
import com.team254.lib.util.Units;
import us.ilite.lib.drivers.Clock;
import us.ilite.robot.modules.DriveMessage;

public class SimDriveHardware implements IDriveHardware {

    private final ILog mLogger = Logger.createLog(DriveHardware.class);

    private ControlMode mLeftControlMode, mRightControlMode;
    private NeutralMode mLeftNeutralMode, mRightNeutralMode;

    private double mLeftVolts, mRightVolts;
    private double mLeftPosInches, mRightPosInches;
    private double mLastLeftVelInches, mLastRightVelInches = 0.0;
    private double mLeftVelInches, mRightVelInches = 0.0;
    private double mLeftAccelInches, mRightAccelInches = 0.0;

    private Clock mClock;
    private double mLastTime = 0.0;
    private DriveController mDriveController;
    private DCMotorTransmission mLeftTransmission, mRightTransmission;

    public SimDriveHardware(DriveController pDriveController, Clock pClock) {
        mDriveController = pDriveController;
        mClock = pClock;
        mLeftTransmission = pDriveController.getLeftTransmission();
        mRightTransmission = pDriveController.getRightTransmission();
    }

    @Override
    public void init() {
        zero();
        mLeftControlMode = mRightControlMode = ControlMode.PercentOutput;
        mLeftNeutralMode = mRightNeutralMode = NeutralMode.Coast;
    }

    @Override
    public void zero() {
        mDriveController.getRobotStateEstimator().reset();

        mLeftPosInches = mRightPosInches = 0.0;

        mLeftVolts = mRightVolts = 0.0;
    }

    public void set(DriveMessage pDriveMessage) {

        mLeftVelInches = setMotor(mLeftTransmission, pDriveMessage.leftControlMode, pDriveMessage.leftOutput, pDriveMessage.leftDemandType, pDriveMessage.leftDemand);
        mRightVelInches = setMotor(mRightTransmission, pDriveMessage.rightControlMode, pDriveMessage.rightOutput, pDriveMessage.rightDemandType, pDriveMessage.rightDemand);
        
    }
    
    public void update(double pNow) {
        double dt = pNow - mLastTime;
        
        mLeftAccelInches = (mLeftVelInches - mLastLeftVelInches) / dt;
        mRightAccelInches = (mRightVelInches - mLastRightVelInches) / dt;
        
        mLeftPosInches = (mLeftVelInches * dt) + (0.5 * mLeftAccelInches * dt * dt);
        mRightPosInches = (mRightVelInches * dt) + (0.5 * mRightAccelInches * dt * dt);

        mLastLeftVelInches = mLeftVelInches;
        mLastRightVelInches = mRightVelInches;

        mLastTime = pNow;
    }

    public double setMotor(DCMotorTransmission pTransmission, ControlMode pControlMode, double pOutput, DemandType pDemandType, double pDemand) {

        double voltage = 0.0;

        switch(pControlMode) {
            case PercentOutput:
                voltage = pOutput * 12.0;
                break;
            case Velocity:
                voltage = pOutput / pTransmission.speed_per_volt();
                break;
        }

        if(pDemandType == DemandType.ArbitraryFeedForward) {
            voltage += pDemand;
        }

        return Units.meters_to_inches(pTransmission.free_speed_at_voltage(voltage));

    }
    
    public Rotation2d getHeading() {
        return mDriveController.getRobotStateEstimator().getRobotState().getLatestFieldToVehiclePose().getRotation();
    }

    public double getLeftInches() {
        return mLeftPosInches;
    }

    public double getRightInches() {
        return mRightPosInches;
    }

    public int getLeftVelTicks() {
        return (int)(mLeftVelInches / SystemSettings.kDriveWheelDiameterInches * SystemSettings.kDriveTicksPerRotation / 10);
    }

    public int getRightVelTicks() {
        return (int)(mRightVelInches / SystemSettings.kDriveWheelDiameterInches * SystemSettings.kDriveTicksPerRotation / 10);
    }

    public double getLeftVelInches() {
        return mLeftVelInches;
    }

    public double getRightVelInches() {
        return mRightVelInches;
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
