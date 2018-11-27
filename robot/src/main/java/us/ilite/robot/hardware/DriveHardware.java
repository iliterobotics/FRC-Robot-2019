package us.ilite.robot.hardware;

import com.ctre.phoenix.ErrorCode;
import com.ctre.phoenix.motorcontrol.*;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.sensors.PigeonIMU;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Talon;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.lib.geometry.Rotation2d;
import us.ilite.common.lib.util.Units;
import us.ilite.lib.drivers.Pigeon;
import us.ilite.lib.drivers.TalonSRXFactory;
import us.ilite.robot.modules.DriveMessage;

public class DriveHardware implements IHardware {

    private final ILog mLogger = Logger.createLog(DriveHardware.class);

    private final PigeonIMU mGyro;

    private final TalonSRX mLeftMaster, mRightMaster, mLeftMiddle, mRightMiddle, mLeftRear, mRightRear;
    private ControlMode mLeftControlMode, mRightControlMode;
    private NeutralMode mLeftNeutralMode, mRightNeutralMode;

    public DriveHardware() {
        mGyro = new PigeonIMU(SystemSettings.kPigeonId);

        mLeftMaster = TalonSRXFactory.createDefaultTalon(SystemSettings.kDriveLeftMasterTalonId);
        mLeftMiddle = TalonSRXFactory.createPermanentSlaveTalon(SystemSettings.kDriveLeftMiddleTalonId, SystemSettings.kDriveLeftMasterTalonId);
        mLeftRear = TalonSRXFactory.createPermanentSlaveTalon(SystemSettings.kDriveLeftRearTalonId, SystemSettings.kDriveLeftMasterTalonId);

        mRightMaster = TalonSRXFactory.createDefaultTalon(SystemSettings.kDriveRightMasterTalonId);
        mRightMiddle = TalonSRXFactory.createPermanentSlaveTalon(SystemSettings.kDriveRightMiddleTalonId, SystemSettings.kDriveRightMasterTalonId);
        mRightRear = TalonSRXFactory.createPermanentSlaveTalon(SystemSettings.kDriveRightRearTalonId, SystemSettings.kDriveRightMasterTalonId);

        configureMaster(mLeftMaster, true);
        configureMaster(mRightMaster, false);

        mLeftMaster.setInverted(false);
        mLeftMiddle.setInverted(false);
        mLeftRear.setInverted(false);

        mRightMaster.setInverted(true);
        mRightMiddle.setInverted(true);
        mRightRear.setInverted(true);

        configTalonForVelocity(mRightMaster);
        configTalonForVelocity(mLeftMaster);
    }

    @Override
    public void init() {
        zero();
        mLeftControlMode = mRightControlMode = ControlMode.PercentOutput;
        mLeftNeutralMode = mRightNeutralMode = NeutralMode.Coast;
    }

    @Override
    public void zero() {
        mGyro.setFusedHeading(Rotation2d.identity().getDegrees(), SystemSettings.kCANTimeoutMs);

        mLeftMaster.setSelectedSensorPosition(0, 0, SystemSettings.kCANTimeoutMs);
        mRightMaster.setSelectedSensorPosition(0, 0, SystemSettings.kCANTimeoutMs);

        mLeftMaster.set(ControlMode.PercentOutput, 0.0);
        mRightMaster.set(ControlMode.PercentOutput, 0.0);
    }

    public void set(DriveMessage pDriveMessage) {

        mLeftControlMode = configForControlMode(mLeftMaster, mLeftControlMode, pDriveMessage.leftControlMode);
        mRightControlMode = configForControlMode(mRightMaster, mRightControlMode, pDriveMessage.rightControlMode);

        mLeftNeutralMode = configForNeutralMode(mLeftMaster, mLeftNeutralMode, pDriveMessage.leftNeutralMode);
        mRightNeutralMode = configForNeutralMode(mRightMaster, mRightNeutralMode, pDriveMessage.rightNeutralMode);

        mLeftMaster.set(mLeftControlMode, pDriveMessage.leftOutput, pDriveMessage.leftDemandType, pDriveMessage.leftDemand);
        mRightMaster.set(mRightControlMode, pDriveMessage.rightOutput, pDriveMessage.rightDemandType, pDriveMessage.rightDemand);
    }

    private ControlMode configForControlMode(TalonSRX pTalon, ControlMode pCurrentControlMode, ControlMode pDesiredControlMode) {
        ControlMode controlMode = pCurrentControlMode;

        if(pCurrentControlMode != pDesiredControlMode) {
            switch(pDesiredControlMode) {
                case PercentOutput:
                    controlMode = ControlMode.PercentOutput;
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

    private NeutralMode configForNeutralMode(TalonSRX pTalon, NeutralMode pCurrentNeutralMode, NeutralMode pDesiredNeutralMode) {
        if(pCurrentNeutralMode != pDesiredNeutralMode) {
            pTalon.setNeutralMode(pDesiredNeutralMode);
        }

        return pDesiredNeutralMode;
    }

    private void configureMaster(TalonSRX talon, boolean pIsLeft) {
        talon.setStatusFramePeriod(StatusFrameEnhanced.Status_2_Feedback0, 5, SystemSettings.kLongCANTimeoutMs);
        final ErrorCode sensorPresent = talon.configSelectedFeedbackSensor(FeedbackDevice
                .QuadEncoder, 0, 100); //primary closed-loop, 100 ms timeout
        if (sensorPresent != ErrorCode.OK) {
            mLogger.error("Could not detect " + (pIsLeft ? "left" : "right") + " encoder: " + sensorPresent);
        }
        talon.setSensorPhase(true);
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

    }

    private void configTalonForPosition(TalonSRX talon) {
        talon.configAllowableClosedloopError(SystemSettings.kDrivePositionLoopSlot, SystemSettings.kDrivePositionTolerance, SystemSettings.kLongCANTimeoutMs);
        talon.config_kP(SystemSettings.kDrivePositionLoopSlot, SystemSettings.kDrivePosition_kP, SystemSettings.kLongCANTimeoutMs);
        talon.config_kI(SystemSettings.kDrivePositionLoopSlot, SystemSettings.kDrivePosition_kI, SystemSettings.kLongCANTimeoutMs);
        talon.config_kD(SystemSettings.kDrivePositionLoopSlot, SystemSettings.kDrivePosition_kD, SystemSettings.kLongCANTimeoutMs);
        talon.config_kF(SystemSettings.kDrivePositionLoopSlot, SystemSettings.kDrivePosition_kD, SystemSettings.kLongCANTimeoutMs);

    }

    private void configTalonForVelocity(TalonSRX talon) {
        talon.configAllowableClosedloopError(SystemSettings.kDriveVelocityLoopSlot, SystemSettings.kDriveVelocityTolerance, SystemSettings.kLongCANTimeoutMs);
        talon.config_kP(SystemSettings.kDriveVelocityLoopSlot, SystemSettings.kDriveVelocity_kP, SystemSettings.kLongCANTimeoutMs);
        talon.config_kI(SystemSettings.kDriveVelocityLoopSlot, SystemSettings.kDriveVelocity_kI, SystemSettings.kLongCANTimeoutMs);
        talon.config_kD(SystemSettings.kDriveVelocityLoopSlot, SystemSettings.kDriveVelocity_kD, SystemSettings.kLongCANTimeoutMs);
        talon.config_kF(SystemSettings.kDriveVelocityLoopSlot, SystemSettings.kDriveVelocity_kF, SystemSettings.kLongCANTimeoutMs);
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
        return Units.ticks_to_inches(mLeftMaster.getSelectedSensorPosition(0));
    }

    public double getRightInches() {
        return Units.ticks_to_inches(mRightMaster.getSelectedSensorPosition(0));
    }

    public double getLeftVelTicks() {
        return mLeftMaster.getSelectedSensorVelocity(0);
    }

    public double getRightVelTicks() {
        return mRightMaster.getSelectedSensorVelocity(0);
    }
    
    public double getLeftVelInches() {
        return Units.ticks_to_fps(mLeftMaster.getSelectedSensorVelocity(0)) / 12.0;
    }

    public double getRightVelInches() {
        return Units.ticks_to_fps(mRightMaster.getSelectedSensorVelocity(0)) / 12.0;
    }

}
