package us.ilite.robot.hardware;

import com.ctre.phoenix.ErrorCode;
import com.ctre.phoenix.motorcontrol.*;
import com.ctre.phoenix.motorcontrol.can.BaseMotorController;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;
import com.ctre.phoenix.sensors.PigeonIMU;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import com.team254.lib.drivers.talon.TalonSRXFactory;
import com.team254.lib.geometry.Rotation2d;

import us.ilite.common.config.SystemSettings;
import us.ilite.common.lib.util.Conversions;
import us.ilite.lib.drivers.ECommonControlMode;
import us.ilite.lib.drivers.IMU;
import us.ilite.lib.drivers.Pigeon;
import us.ilite.robot.modules.DriveMessage;

/**
 * Provides an interface between high-level planning and logic in Drive and
 * Talon SRX configuration and control. We might put our motor models here too -
 * it would make a ton of sense, and we could just call setVelocity() or
 * setAcceleration in Drive
 */
public class SrxDriveHardware implements IDriveHardware {

    private final ILog mLogger = Logger.createLog(SrxDriveHardware.class);

    private IMU mGyro;

    private final TalonSRX mLeftMaster, mRightMaster;
    private final VictorSPX mLeftMiddle, mRightMiddle, mLeftRear, mRightRear;
    private ControlMode mLeftControlMode, mRightControlMode;
    private NeutralMode mLeftNeutralMode, mRightNeutralMode;

    public SrxDriveHardware() {
        mGyro = new Pigeon(new PigeonIMU(SystemSettings.kPigeonId), SystemSettings.kGyroCollisionThreshold);
        // mGyro = new NavX(SerialPort.Port.kMXP);

        mLeftMaster = TalonSRXFactory.createDefaultTalon(SystemSettings.kDriveLeftMasterTalonId);
        mLeftMiddle = TalonSRXFactory.createPermanentSlaveVictor(SystemSettings.kDriveLeftMiddleTalonId, mLeftMaster);
        mLeftRear = TalonSRXFactory.createPermanentSlaveVictor(SystemSettings.kDriveLeftRearTalonId, mLeftMaster);

        mRightMaster = TalonSRXFactory.createDefaultTalon(SystemSettings.kDriveRightMasterTalonId);
        mRightMiddle = TalonSRXFactory.createPermanentSlaveVictor(SystemSettings.kDriveRightMiddleTalonId, mRightMaster);
        mRightRear = TalonSRXFactory.createPermanentSlaveVictor(SystemSettings.kDriveRightRearTalonId, mRightMaster);

        configureMaster(mLeftMaster, true);
        configureMotor(mLeftMaster);
        configureMotor(mLeftMiddle);
        configureMotor(mLeftRear);

        configureMaster(mRightMaster, false);
        configureMotor(mRightMaster);
        configureMotor(mRightMiddle);
        configureMotor(mRightRear);

        mLeftMaster.setInverted(true);
        mLeftMiddle.setInverted(true);
        mLeftRear.setInverted(true);

        mRightMaster.setInverted(false);
        mRightMiddle.setInverted(false);
        mRightRear.setInverted(false);

        mLeftMaster.setSensorPhase(false);
        mRightMaster.setSensorPhase(false);

        reloadGains(mLeftMaster);
        reloadGains(mRightMaster);

    }

    @Override
    public void init() {
        zero();
        mLeftControlMode = mRightControlMode = ControlMode.PercentOutput;
        mLeftNeutralMode = mRightNeutralMode = NeutralMode.Brake;

        set(DriveMessage.kNeutral);
    }

    @Override
    public void zero() {
        mGyro.zeroAll();

        mLeftMaster.setSelectedSensorPosition(0, 0, SystemSettings.kCANTimeoutMs);
        mRightMaster.setSelectedSensorPosition(0, 0, SystemSettings.kCANTimeoutMs);

        // Bypass state machine in set() and configure directly
        configTalonForPercentOutput(mLeftMaster);
        configTalonForPercentOutput(mRightMaster);
        setNeutralMode(NeutralMode.Brake, mLeftMaster, mLeftMiddle, mLeftRear);
        setNeutralMode(NeutralMode.Brake, mRightMaster, mRightMiddle, mRightRear);

        mLeftMaster.set(ControlMode.PercentOutput, 0.0);
        mRightMaster.set(ControlMode.PercentOutput, 0.0);
    }

    public void set(DriveMessage pDriveMessage) {

        mLeftControlMode = configForControlMode(mLeftMaster, mLeftControlMode, pDriveMessage.leftControlMode.kCtreControlMode);
        mRightControlMode = configForControlMode(mRightMaster, mRightControlMode, pDriveMessage.rightControlMode.kCtreControlMode);

        mLeftNeutralMode = configForNeutralMode(mLeftNeutralMode, pDriveMessage.leftNeutralMode.kCtreNeutralMode, mLeftMaster, mLeftMiddle, mLeftRear);
        mRightNeutralMode = configForNeutralMode(mRightNeutralMode, pDriveMessage.rightNeutralMode.kCtreNeutralMode, mRightMaster, mRightMiddle, mRightRear);

        mLeftMaster.set(mLeftControlMode, pDriveMessage.leftOutput, DemandType.ArbitraryFeedForward, pDriveMessage.leftDemand);
        mRightMaster.set(mRightControlMode, pDriveMessage.rightOutput, DemandType.ArbitraryFeedForward, pDriveMessage.rightDemand);
    }

    /**
     * Allows external users to request that our control mode be pre-configured instead of configuring on the fly.
     * @param pControlMode
     */
    public void configureMode(ECommonControlMode pControlMode) {
        mLeftControlMode = configForControlMode(mLeftMaster, mLeftControlMode, pControlMode.kCtreControlMode);
        mRightControlMode = configForControlMode(mRightMaster, mRightControlMode, pControlMode.kCtreControlMode);
    }

    @Override
    public void setImu(IMU pImu) {
        mGyro = pImu;
    }

    public IMU getImu() {
        return mGyro;
    }

    private ControlMode configForControlMode(TalonSRX pTalon, ControlMode pCurrentControlMode, ControlMode pDesiredControlMode) {
        ControlMode controlMode = pCurrentControlMode;

        if(pCurrentControlMode != pDesiredControlMode) {
            switch(pDesiredControlMode) {
                case PercentOutput:
                    controlMode = ControlMode.PercentOutput;
                    configTalonForPercentOutput(pTalon);
                    break;
                case Position:
                    controlMode = ControlMode.Position;
                    configTalonForPosition(pTalon);
                    break;
                case MotionMagic:
                    controlMode = ControlMode.MotionMagic;
                    configTalonForMotionMagic(pTalon);
                    break;
                case Velocity:
                    controlMode = ControlMode.Velocity;
                    configTalonForVelocity(pTalon);
                    break;
                default:
                    mLogger.error("Unimplemented control mode - defaulting to PercentOutput.");
                    controlMode = ControlMode.PercentOutput;
                    break;
            }
        }

        return controlMode;
    }

    private NeutralMode configForNeutralMode(NeutralMode pCurrentNeutralMode, NeutralMode pDesiredNeutralMode, BaseMotorController ... pTalons) {
        if(pCurrentNeutralMode != pDesiredNeutralMode) {
            setNeutralMode(pDesiredNeutralMode, pTalons);
        }

        return pDesiredNeutralMode;
    }

    private void setNeutralMode(NeutralMode pNeutralMode, BaseMotorController ... pTalons) {
        for(BaseMotorController talon : pTalons) {
            mLogger.info("Setting neutral mode to: ", pNeutralMode.name(), " for Talon ID ", talon.getDeviceID());
            talon.setNeutralMode(pNeutralMode);
        }
    }

    private void configureMaster(TalonSRX talon, boolean pIsLeft) {
        talon.setStatusFramePeriod(StatusFrameEnhanced.Status_2_Feedback0, 5, SystemSettings.kLongCANTimeoutMs);
        final ErrorCode sensorPresent = talon.configSelectedFeedbackSensor(FeedbackDevice
                .QuadEncoder, 0, 100); //primary closed-loop, 100 ms timeout
        if (sensorPresent != ErrorCode.OK) {
            mLogger.error("Could not detect " + (pIsLeft ? "left" : "right") + " encoder: " + sensorPresent);
        }

        talon.enableCurrentLimit(true);
        talon.configContinuousCurrentLimit(SystemSettings.kDriveCurrentLimitAmps, SystemSettings.kLongCANTimeoutMs);
        talon.configPeakCurrentLimit(SystemSettings.kDriveCurrentLimitAmps, SystemSettings.kLongCANTimeoutMs);
        talon.configPeakCurrentDuration(SystemSettings.kDriveCurrentLimitTriggerDurationMs, SystemSettings.kLongCANTimeoutMs);
    }

    private void configureMotor(BaseMotorController motorController) {
        motorController.enableVoltageCompensation(true);
        motorController.configVoltageCompSaturation(12.0, SystemSettings.kLongCANTimeoutMs);
        motorController.configVelocityMeasurementPeriod(VelocityMeasPeriod.Period_100Ms, SystemSettings.kLongCANTimeoutMs);
        motorController.configVelocityMeasurementWindow(64, SystemSettings.kLongCANTimeoutMs);
        motorController.configOpenloopRamp(SystemSettings.kDriveMinOpenLoopVoltageRampRate, SystemSettings.kLongCANTimeoutMs);
        motorController.configClosedloopRamp(SystemSettings.kDriveClosedLoopVoltageRampRate, SystemSettings.kLongCANTimeoutMs);
        // motorController.configNeutralDeadband(0.04, 0);
    }

    private void configTalonForPercentOutput(TalonSRX talon) {
        // talon.configNeutralDeadband(0.04, 0);
    }

    private void configTalonForPosition(TalonSRX talon) {
        talon.selectProfileSlot(SystemSettings.kDrivePositionLoopSlot, 0);
    }

    private void configTalonForVelocity(TalonSRX talon) {
        mLogger.info("Configuring Talon ID ", talon.getDeviceID(), " for velocity mode");

        talon.selectProfileSlot(SystemSettings.kDriveVelocityLoopSlot, 0);
        talon.configNeutralDeadband(0.0, 0);
    }

    private void reloadGains(TalonSRX talon) {
        mLogger.info("Reloading gains for Talon ID ", talon.getDeviceID());

        talon.configAllowableClosedloopError(SystemSettings.kDriveVelocityLoopSlot, SystemSettings.kDriveVelocityTolerance, SystemSettings.kLongCANTimeoutMs);
        talon.config_kP(SystemSettings.kDriveVelocityLoopSlot, SystemSettings.kDriveVelocity_kP, SystemSettings.kLongCANTimeoutMs);
        talon.config_kI(SystemSettings.kDriveVelocityLoopSlot, SystemSettings.kDriveVelocity_kI, SystemSettings.kLongCANTimeoutMs);
        talon.config_kD(SystemSettings.kDriveVelocityLoopSlot, SystemSettings.kDriveVelocity_kD, SystemSettings.kLongCANTimeoutMs);
        talon.config_kF(SystemSettings.kDriveVelocityLoopSlot, SystemSettings.kDriveVelocity_kF, SystemSettings.kLongCANTimeoutMs);

        talon.configAllowableClosedloopError(SystemSettings.kDrivePositionLoopSlot, SystemSettings.kDrivePositionTolerance, SystemSettings.kLongCANTimeoutMs);
        talon.config_kP(SystemSettings.kDrivePositionLoopSlot, SystemSettings.kDrivePosition_kP, SystemSettings.kLongCANTimeoutMs);
        talon.config_kI(SystemSettings.kDrivePositionLoopSlot, SystemSettings.kDrivePosition_kI, SystemSettings.kLongCANTimeoutMs);
        talon.config_kD(SystemSettings.kDrivePositionLoopSlot, SystemSettings.kDrivePosition_kD, SystemSettings.kLongCANTimeoutMs);
        talon.config_kF(SystemSettings.kDrivePositionLoopSlot, SystemSettings.kDrivePosition_kD, SystemSettings.kLongCANTimeoutMs);
    }

    private void configTalonForMotionMagic(TalonSRX talon) {
        configTalonForVelocity(talon);

        talon.configMotionCruiseVelocity(SystemSettings.kDriveMotionMagicCruiseVelocity, SystemSettings.kLongCANTimeoutMs);
        talon.configMotionAcceleration(SystemSettings.kDriveMotionMagicMaxAccel, SystemSettings.kLongCANTimeoutMs);
    }

    public Rotation2d getHeading() {
        return mGyro.getHeading();
    }

    public double getLeftInches() {
        return Conversions.ticksToInches(mLeftMaster.getSelectedSensorPosition(0));
    }

    public double getRightInches() {
        return Conversions.ticksToInches(mRightMaster.getSelectedSensorPosition(0));
    }

    public double getLeftVelTicks() {
        return mLeftMaster.getSelectedSensorVelocity(0);
    }

    public double getRightVelTicks() {
        return mRightMaster.getSelectedSensorVelocity(0);
    }

    public double getLeftTarget() {
        return mLeftMaster.getClosedLoopTarget();
    }

    public double getRightTarget() {
        return mRightMaster.getClosedLoopTarget();
    }

    public double getLeftVelInches() {
        return Conversions.ticksPer100msToRadiansPerSecond(mLeftMaster.getSelectedSensorVelocity());
    }

    public double getRightVelInches() {
        return Conversions.ticksPer100msToRadiansPerSecond(mRightMaster.getSelectedSensorVelocity());
    }

    @Override
    public double getLeftCurrent() {
        return mLeftMaster.getOutputCurrent();
    }

    @Override
    public double getRightCurrent() {
        return mRightMaster.getOutputCurrent();
    }

    @Override
    public double getLeftVoltage() {
        return mLeftMaster.getMotorOutputVoltage();
    }

    @Override
    public double getRightVoltage() {
        return mRightMaster.getMotorOutputVoltage();
    }

    @Override
    public void setOpenLoopRampRate(double pOpenLoopRampRate) {
        mLeftMaster.configOpenloopRamp(pOpenLoopRampRate);
        mRightMaster.configOpenloopRamp(pOpenLoopRampRate);
    }

    @Override
    public boolean checkHardware() {

        // TODO Implement testing for VictorSPX
        // CheckerConfigBuilder checkerConfigBuilder = new CheckerConfigBuilder();
        // checkerConfigBuilder.setCurrentFloor(2);
        // checkerConfigBuilder.setCurrentEpsilon(2.0);
        // checkerConfigBuilder.setRPMFloor(1500);
        // checkerConfigBuilder.setRPMEpsilon(250);
        // checkerConfigBuilder.setRPMSupplier(()->mLeftMaster.getSelectedSensorVelocity(0));

        // boolean leftSide = TalonSRXChecker.CheckTalons(Drive.class,
        //         Arrays.asList(new TalonSRXChecker.TalonSRXConfig("left_master", mLeftMaster),
        //             new TalonSRXChecker.TalonSRXConfig("left_slave", mLeftRear)),
        //         checkerConfigBuilder.build());

        // checkerConfigBuilder.setRPMSupplier(()->mRightMaster.getSelectedSensorVelocity(0));
        
        // boolean rightSide = TalonSRXChecker.CheckTalons(Drive.class,
        //         Arrays.asList(new TalonSRXChecker.TalonSRXConfig("right_master", mRightMaster),
        //                 new TalonSRXChecker.TalonSRXConfig("right_slave", mRightRear)), 
        //         checkerConfigBuilder.build());
        // return leftSide && rightSide;
        return true;
    }

}
