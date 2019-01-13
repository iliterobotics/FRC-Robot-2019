package us.ilite.robot.hardware;

import java.util.ArrayList;

import com.ctre.phoenix.ErrorCode;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced;
import com.ctre.phoenix.motorcontrol.VelocityMeasPeriod;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.sensors.PigeonIMU;
import com.ctre.phoenix.sensors.PigeonIMU_StatusFrame;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import com.team254.lib.geometry.Rotation2d;

import us.ilite.common.config.SystemSettings;
import us.ilite.common.lib.util.Conversions;
import com.team254.lib.drivers.talon.TalonSRXChecker;
import com.team254.lib.drivers.talon.TalonSRXFactory;
import us.ilite.robot.modules.Drive;
import us.ilite.robot.modules.DriveMessage;

/**
 * Provides an interface between high-level planning and logic in Drive and Talon SRX configuration and control.
 * We might put our motor models here too - it would make a ton of sense, and we could just call setVelocity() or setAcceleration in Drive
 */
public class DriveHardware implements IDriveHardware {

    private final ILog mLogger = Logger.createLog(DriveHardware.class);

    private final PigeonIMU mGyro;

    private final TalonSRX mLeftMaster, mRightMaster, mLeftRear, mRightRear;
    private ControlMode mLeftControlMode, mRightControlMode;
    private NeutralMode mLeftNeutralMode, mRightNeutralMode;

    public DriveHardware() {
        mGyro = new PigeonIMU(SystemSettings.kPigeonId);
        mGyro.setStatusFramePeriod(PigeonIMU_StatusFrame.CondStatus_9_SixDeg_YPR, 5, SystemSettings.kLongCANTimeoutMs);
        mGyro.setStatusFramePeriod(PigeonIMU_StatusFrame.CondStatus_6_SensorFusion, 5, SystemSettings.kLongCANTimeoutMs);

        mLeftMaster = TalonSRXFactory.createDefaultTalon(SystemSettings.kDriveLeftMasterTalonId);
        mLeftRear = TalonSRXFactory.createPermanentSlaveTalon(SystemSettings.kDriveLeftRearTalonId, SystemSettings.kDriveLeftMasterTalonId);

        mRightMaster = TalonSRXFactory.createDefaultTalon(SystemSettings.kDriveRightMasterTalonId);
        mRightRear = TalonSRXFactory.createPermanentSlaveTalon(SystemSettings.kDriveRightRearTalonId, SystemSettings.kDriveRightMasterTalonId);

        configureMaster(mLeftMaster, true);
        configureMaster(mRightMaster, false);
        configureMaster(mLeftRear, true);
        configureMaster(mRightRear, false);

        mLeftMaster.setInverted(false);
        mLeftRear.setInverted(false);

        mRightMaster.setInverted(true);
        mRightRear.setInverted(true);

        mLeftMaster.setSensorPhase(false);
        mRightMaster.setSensorPhase(false);

        reloadGains(mLeftMaster);
        reloadGains(mRightMaster);

    }

    @Override
    public void init() {
//        reloadGains(mLeftMaster);
//        reloadGains(mRightMaster);

        zero();
        mLeftControlMode = mRightControlMode = ControlMode.PercentOutput;
        mLeftNeutralMode = mRightNeutralMode = NeutralMode.Brake;

        // Bypass state machine in set() and configure directly
        configTalonForPercentOutput(mLeftMaster);
        configTalonForPercentOutput(mRightMaster);
        setNeutralMode(mLeftNeutralMode, mRightMaster, mRightRear);
        setNeutralMode(mLeftNeutralMode, mLeftMaster, mRightMaster);

        set(DriveMessage.kNeutral);
    }

    @Override
    public void zero() {
        mGyro.setFusedHeading(Rotation2d.identity().getDegrees(), SystemSettings.kCANTimeoutMs);
        mGyro.setYaw(Rotation2d.identity().getDegrees(), SystemSettings.kCANTimeoutMs);

        mLeftMaster.setSelectedSensorPosition(0, 0, SystemSettings.kCANTimeoutMs);
        mRightMaster.setSelectedSensorPosition(0, 0, SystemSettings.kCANTimeoutMs);

        mLeftMaster.set(ControlMode.PercentOutput, 0.0);
        mRightMaster.set(ControlMode.PercentOutput, 0.0);
    }

    public void set(DriveMessage pDriveMessage) {

        mLeftControlMode = configForControlMode(mLeftMaster, mLeftControlMode, pDriveMessage.leftControlMode);
        mRightControlMode = configForControlMode(mRightMaster, mRightControlMode, pDriveMessage.rightControlMode);

        mLeftNeutralMode = configForNeutralMode(mLeftNeutralMode, pDriveMessage.leftNeutralMode, mLeftMaster, mLeftRear);
        mRightNeutralMode = configForNeutralMode(mRightNeutralMode, pDriveMessage.rightNeutralMode, mRightMaster, mRightRear);

        mLeftMaster.set(mLeftControlMode, pDriveMessage.leftOutput, pDriveMessage.leftDemandType, pDriveMessage.leftDemand);
        mRightMaster.set(mRightControlMode, pDriveMessage.rightOutput, pDriveMessage.rightDemandType, pDriveMessage.rightDemand);
    }

    /**
     * Allows external users to request that our control mode be pre-configured instead of configuring on the fly.
     * @param pControlMode
     */
    public void configureMode(ControlMode pControlMode) {
        mLeftControlMode = configForControlMode(mLeftMaster, mLeftControlMode, pControlMode);
        mRightControlMode = configForControlMode(mRightMaster, mRightControlMode, pControlMode);
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

    private NeutralMode configForNeutralMode(NeutralMode pCurrentNeutralMode, NeutralMode pDesiredNeutralMode, TalonSRX ... pTalons) {
        if(pCurrentNeutralMode != pDesiredNeutralMode) {
            setNeutralMode(pDesiredNeutralMode, pTalons);
        }

        return pDesiredNeutralMode;
    }

    private void setNeutralMode(NeutralMode pNeutralMode, TalonSRX ... pTalons) {
        for(TalonSRX talon : pTalons) {
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
        talon.enableVoltageCompensation(true);
        talon.configVoltageCompSaturation(12.0, SystemSettings.kLongCANTimeoutMs);
        talon.configVelocityMeasurementPeriod(VelocityMeasPeriod.Period_50Ms, SystemSettings.kLongCANTimeoutMs);
        talon.configVelocityMeasurementWindow(1, SystemSettings.kLongCANTimeoutMs);
        talon.configOpenloopRamp(SystemSettings.kDriveOpenLoopVoltageRampRate, SystemSettings.kLongCANTimeoutMs);
        talon.configClosedloopRamp(SystemSettings.kDriveClosedLoopVoltageRampRate, SystemSettings.kLongCANTimeoutMs);
        talon.configContinuousCurrentLimit(SystemSettings.kDriveCurrentLimitAmps, SystemSettings.kLongCANTimeoutMs);
        talon.configNeutralDeadband(0.04, 0);
    }

    private void configTalonForPercentOutput(TalonSRX talon) {
        talon.configNeutralDeadband(0.04, 0);
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

        talon.configMotionCruiseVelocity(SystemSettings.kDriveMotionMagicVelocityFeedforward, SystemSettings.kLongCANTimeoutMs);
        talon.configMotionAcceleration(SystemSettings.kDriveMotionMagicAccelFeedforward, SystemSettings.kLongCANTimeoutMs);
    }

    public Rotation2d getHeading() {
        return Rotation2d.fromDegrees(mGyro.getFusedHeading());
    }

    public double getLeftInches() {
        return Conversions.ticksToInches(mLeftMaster.getSelectedSensorPosition(0));
    }

    public double getRightInches() {
        return Conversions.ticksToInches(mRightMaster.getSelectedSensorPosition(0));
    }

    public int getLeftVelTicks() {
        return mLeftMaster.getSelectedSensorVelocity(0);
    }

    public int getRightVelTicks() {
        return mRightMaster.getSelectedSensorVelocity(0);
    }
    
    public double getLeftVelInches() {
        return Conversions.ticksPer100msToInchesPerSecond(mLeftMaster.getSelectedSensorVelocity(0));
    }

    public double getRightVelInches() {
        return Conversions.ticksPer100msToInchesPerSecond(mRightMaster.getSelectedSensorVelocity(0));
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
    public boolean checkHardware() {
        boolean leftSide = TalonSRXChecker.CheckTalons(Drive.class,
                new ArrayList<TalonSRXChecker.TalonSRXConfig>() {
                    {
                        add(new TalonSRXChecker.TalonSRXConfig("left_master", mLeftMaster));
                        add(new TalonSRXChecker.TalonSRXConfig("left_slave", mLeftRear));
                    }
                }, new TalonSRXChecker.CheckerConfig() {
                    {
                        mCurrentFloor = 2;
                        mCurrentEpsilon = 2.0;
                        mRPMFloor = 1500;
                        mRPMEpsilon = 250;
                        mRPMSupplier = () -> mLeftMaster.getSelectedSensorVelocity(0);
                    }
                });
        boolean rightSide = TalonSRXChecker.CheckTalons(Drive.class,
                new ArrayList<TalonSRXChecker.TalonSRXConfig>() {
                    {
                        add(new TalonSRXChecker.TalonSRXConfig("right_master", mRightMaster));
                        add(new TalonSRXChecker.TalonSRXConfig("right_slave", mRightRear));
                    }
                }, new TalonSRXChecker.CheckerConfig() {
                    {
                        mCurrentFloor = 2;
                        mRPMFloor = 1500;
                        mCurrentEpsilon = 2.0;
                        mRPMEpsilon = 250;
                        mRPMSupplier = () -> mRightMaster.getSelectedSensorVelocity(0);
                    }
                });
        return leftSide && rightSide;
    }

}
