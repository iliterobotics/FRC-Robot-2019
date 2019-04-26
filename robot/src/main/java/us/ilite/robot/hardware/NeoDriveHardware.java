package us.ilite.robot.hardware;

import com.ctre.phoenix.sensors.PigeonIMU;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import com.revrobotics.CANPIDController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel;
import com.revrobotics.ControlType;
import com.team254.lib.geometry.Rotation2d;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.lib.util.Conversions;
import us.ilite.common.lib.util.RangeScale;
import us.ilite.lib.drivers.ECommonControlMode;
import us.ilite.lib.drivers.IMU;
import us.ilite.lib.drivers.Pigeon;
import us.ilite.lib.drivers.SparkMaxFactory;
import us.ilite.robot.modules.DriveMessage;
import us.ilite.robot.modules.Elevator;

public class NeoDriveHardware implements IDriveHardware {

    private final ILog mLogger = Logger.createLog(SrxDriveHardware.class);
    private final double kGearRatio;

    private IMU mGyro;

    private final CANSparkMax mLeftMaster, mRightMaster, mLeftMiddle, mRightMiddle, mLeftRear, mRightRear;
    private ControlType mLeftControlMode, mRightControlMode;
    private CANSparkMax.IdleMode mLeftNeutralMode, mRightNeutralMode;
    private int mPidSlot = SystemSettings.kDriveVelocityLoopSlot;
    private double mCurrentOpenLoopRampRate = SystemSettings.kDriveMinOpenLoopVoltageRampRate;
    private RangeScale mRangeScale;

    public NeoDriveHardware(double pGearRatio) {
        kGearRatio = pGearRatio;
        mGyro = new Pigeon(new PigeonIMU(SystemSettings.kPigeonId), SystemSettings.kGyroCollisionThreshold);
        // mGyro = new NavX(SerialPort.Port.kMXP);

        mLeftMaster = SparkMaxFactory.createDefaultSparkMax(SystemSettings.kDriveLeftMasterTalonId, CANSparkMaxLowLevel.MotorType.kBrushless);
        mLeftMiddle = SparkMaxFactory.createDefaultSparkMax(SystemSettings.kDriveLeftMiddleTalonId, CANSparkMaxLowLevel.MotorType.kBrushless);
        mLeftRear = SparkMaxFactory.createDefaultSparkMax(SystemSettings.kDriveLeftRearTalonId, CANSparkMaxLowLevel.MotorType.kBrushless);

        mRightMaster = SparkMaxFactory.createDefaultSparkMax(SystemSettings.kDriveRightMasterTalonId, CANSparkMaxLowLevel.MotorType.kBrushless);
        mRightMiddle = SparkMaxFactory.createDefaultSparkMax(SystemSettings.kDriveRightMiddleTalonId, CANSparkMaxLowLevel.MotorType.kBrushless);
        mRightRear = SparkMaxFactory.createDefaultSparkMax(SystemSettings.kDriveRightRearTalonId, CANSparkMaxLowLevel.MotorType.kBrushless);

        configureMaster(true, mLeftMaster, mLeftMiddle, mLeftRear);
        configureMotor(mLeftMaster, mLeftMiddle, mLeftRear);

        configureMaster(false, mRightMaster, mRightMiddle, mRightRear);
        configureMotor(mRightMaster, mRightMiddle, mRightRear);

        mLeftMaster.setInverted(true);
        mLeftMiddle.setInverted(true);
        mLeftRear.setInverted(true);

        mRightMaster.setInverted(false);
        mRightMiddle.setInverted(false);
        mRightRear.setInverted(false);

        // Invert sensor readings by multiplying by 1 or -1
        mLeftMaster.getEncoder().setPositionConversionFactor(1.0 * kGearRatio);
        mLeftMiddle.getEncoder().setPositionConversionFactor(1.0 * kGearRatio);
        mLeftRear.getEncoder().setPositionConversionFactor(1.0 * kGearRatio);

        mRightMaster.getEncoder().setPositionConversionFactor(1.0 * kGearRatio);
        mRightMiddle.getEncoder().setPositionConversionFactor(1.0 * kGearRatio);
        mRightRear.getEncoder().setPositionConversionFactor(1.0 * kGearRatio);

        reloadVelocityGains(mLeftMaster, mLeftMiddle, mLeftRear);
        reloadVelocityGains(mRightMaster, mRightMiddle, mRightRear);

        mRangeScale = new RangeScale(SystemSettings.kDriveMinOpenLoopVoltageRampRate,
                SystemSettings.kDriveMaxOpenLoopVoltageRampRate,
                Elevator.EElevatorPosition.CARGO_BOTTOM.getEncoderRotations(),
                Elevator.EElevatorPosition.CARGO_TOP.getEncoderRotations());
    }

    @Override
    public void init() {
        zero();
        mLeftControlMode = mRightControlMode = ControlType.kDutyCycle;
        mLeftNeutralMode = mRightNeutralMode = CANSparkMax.IdleMode.kBrake;

        set(DriveMessage.kNeutral);
    }

    @Override
    public void zero() {
        mGyro.zeroAll();

        mLeftMaster.getEncoder().setPosition(0.0);
        mRightMaster.getEncoder().setPosition(0.0);

        // Bypass state machine in set() and configure directly
        configSparkForPercentOutput(mLeftMaster, mLeftMiddle, mLeftRear);
        configSparkForPercentOutput(mRightMaster, mRightMiddle, mRightRear);
        setNeutralMode(CANSparkMax.IdleMode.kBrake, mLeftMaster, mLeftMiddle, mLeftRear);
        setNeutralMode(CANSparkMax.IdleMode.kBrake, mRightMaster, mRightMiddle, mRightRear);

        mLeftMaster.set(0.0);
        mLeftMiddle.set(0.0);
        mLeftRear.set(0.0);
        mRightMaster.set(0.0);
        mRightMiddle.set(0.0);
        mRightRear.set(0.0);
    }

    public void set(DriveMessage pDriveMessage) {

        mLeftControlMode = configForControlMode(mLeftControlMode, pDriveMessage.leftControlMode.kRevControlType, mLeftMaster, mLeftMiddle, mLeftRear);
        mRightControlMode = configForControlMode(mRightControlMode, pDriveMessage.rightControlMode.kRevControlType, mRightMaster, mRightMiddle, mRightRear);

        mLeftNeutralMode = configForNeutralMode(mLeftNeutralMode, pDriveMessage.leftNeutralMode.kRevIdleMode, mLeftMaster, mLeftMiddle, mLeftRear);
        mRightNeutralMode = configForNeutralMode(mRightNeutralMode, pDriveMessage.rightNeutralMode.kRevIdleMode, mRightMaster, mRightMiddle, mRightRear);

        mLeftMaster.getPIDController().setReference(pDriveMessage.leftOutput, mLeftControlMode, mPidSlot, pDriveMessage.leftDemand);
        mLeftMiddle.getPIDController().setReference(pDriveMessage.leftOutput, mLeftControlMode, mPidSlot, pDriveMessage.leftDemand);
        mLeftRear.getPIDController().setReference(pDriveMessage.leftOutput, mLeftControlMode, mPidSlot, pDriveMessage.leftDemand);

        mRightMaster.getPIDController().setReference(pDriveMessage.rightOutput, mRightControlMode, mPidSlot, pDriveMessage.rightDemand);
        mRightMiddle.getPIDController().setReference(pDriveMessage.rightOutput, mRightControlMode, mPidSlot, pDriveMessage.rightDemand);
        mRightRear.getPIDController().setReference(pDriveMessage.rightOutput, mRightControlMode, mPidSlot, pDriveMessage.rightDemand);

    }

    /**
     * Allows external users to request that our control mode be pre-configured instead of configuring on the fly.
     * @param pControlMode
     */
    public void configureMode(ECommonControlMode pControlMode) {
        mLeftControlMode = configForControlMode(mLeftControlMode, pControlMode.kRevControlType, mLeftMaster, mLeftMiddle, mLeftRear);
        mRightControlMode = configForControlMode(mRightControlMode, pControlMode.kRevControlType, mRightMaster, mRightMiddle, mRightRear);
    }

    @Override
    public void setImu(IMU pImu) {
        mGyro = pImu;
    }

    public IMU getImu() {
        return mGyro;
    }

    private ControlType configForControlMode(ControlType pCurrentControlMode, ControlType pDesiredControlMode, CANSparkMax ... pSparkMaxes) {
        for(CANSparkMax spark : pSparkMaxes) {
            ControlType controlMode = pCurrentControlMode;

            if(pCurrentControlMode != pDesiredControlMode) {
                switch(pDesiredControlMode) {
                    case kDutyCycle:
                        controlMode = ControlType.kDutyCycle;
                        configSparkForPercentOutput(spark);
                        break;
                    case kSmartMotion:
                        controlMode = ControlType.kSmartMotion;
                        configSparkForSmartMotion(spark);
                        break;
                    case kVelocity:
                        controlMode = ControlType.kVelocity;
                        configSparkForVelocity(spark);
                        break;
                    default:
                        mLogger.error("Unimplemented control mode - defaulting to PercentOutput.");
                        controlMode = ControlType.kDutyCycle;
                        break;
                }
            }
        }

        return pDesiredControlMode;
    }

    private CANSparkMax.IdleMode configForNeutralMode(CANSparkMax.IdleMode pCurrentNeutralMode, CANSparkMax.IdleMode pDesiredNeutralMode, CANSparkMax... pSparkMaxes) {
        if(pCurrentNeutralMode != pDesiredNeutralMode) {
            setNeutralMode(pDesiredNeutralMode, pSparkMaxes);
        }

        return pDesiredNeutralMode;
    }

    private void setNeutralMode(CANSparkMax.IdleMode pNeutralMode, CANSparkMax ... pSparkMaxes) {
        for(CANSparkMax sparkMax : pSparkMaxes) {
            mLogger.info("Setting neutral mode to: ", pNeutralMode.name(), " for Talon ID ", sparkMax.getDeviceId());
            sparkMax.setIdleMode(pNeutralMode);
        }
    }

    private void configureMaster(boolean pIsLeft, CANSparkMax ... pSparkMaxes) {
        for(CANSparkMax sparkMax : pSparkMaxes) {
            // Velocity, temperature, voltage, and current according the REV docs
            sparkMax.setPeriodicFramePeriod(CANSparkMaxLowLevel.PeriodicFrame.kStatus1, 5);
            // Position according to REV docs
            sparkMax.setPeriodicFramePeriod(CANSparkMaxLowLevel.PeriodicFrame.kStatus2, 5);

            sparkMax.setSmartCurrentLimit(SystemSettings.kDriveCurrentLimitAmps);
            sparkMax.setSecondaryCurrentLimit(SystemSettings.kDriveCurrentLimitAmps);
            // Set a peak current limit duration??
        }
    }

    private void configureMotor(CANSparkMax ... pSparkMaxes) {
        for(CANSparkMax motorController : pSparkMaxes) {
            /*
            TODO Disabled voltage comp for now because of:
            https://www.chiefdelphi.com/t/sparkmax-voltage-compensation/350540/5
             */
    //        motorController.enableVoltageCompensation(12.0);
            // No velocity measurement filter
            motorController.setOpenLoopRampRate(SystemSettings.kDriveMaxOpenLoopVoltageRampRate);
            motorController.setClosedLoopRampRate(SystemSettings.kDriveClosedLoopVoltageRampRate);
            // motorController.configNeutralDeadband(0.04, 0);
        }
    }

    private void configSparkForPercentOutput(CANSparkMax ... pSparkMax) {
//        for(CANSparkMax s : pSparkMax) {
//             talon.configNeutralDeadband(0.04, 0);
//        }
    }

    private void configSparkForVelocity(CANSparkMax ... pSparkMaxes) {

        for(CANSparkMax spark : pSparkMaxes) {
            mPidSlot = SystemSettings.kDriveVelocityLoopSlot;
            mLogger.info("Configuring Spark ID ", spark.getDeviceId(), " for velocity mode");
        }

    }


    private void reloadVelocityGains(CANSparkMax ... pSparkMaxes) {

        for(CANSparkMax spark : pSparkMaxes) {
            mLogger.info("Reloading gains for Talon ID ", spark.getDeviceId());

            CANPIDController sparkMaxPid = spark.getPIDController();

            sparkMaxPid.setSmartMotionAllowedClosedLoopError(SystemSettings.kDriveVelocityTolerance, SystemSettings.kDriveVelocityLoopSlot);
            sparkMaxPid.setP(SystemSettings.kDriveVelocity_kP, SystemSettings.kDriveVelocityLoopSlot);
            sparkMaxPid.setI(SystemSettings.kDriveVelocity_kI, SystemSettings.kDriveVelocityLoopSlot);
            sparkMaxPid.setD(SystemSettings.kDriveVelocity_kD, SystemSettings.kDriveVelocityLoopSlot);
            sparkMaxPid.setFF(SystemSettings.kDriveVelocity_kF, SystemSettings.kDriveVelocityLoopSlot);
        }

    }

    private void configSparkForSmartMotion(CANSparkMax talon) {
        configSparkForVelocity(talon);

        talon.getPIDController().setSmartMotionMaxVelocity(SystemSettings.kDriveMotionMagicCruiseVelocity, SystemSettings.kLongCANTimeoutMs);
        talon.getPIDController().setSmartMotionMaxAccel(SystemSettings.kDriveMotionMagicMaxAccel, SystemSettings.kLongCANTimeoutMs);
    }

    public Rotation2d getHeading() {
        return mGyro.getHeading();
    }

    public double getLeftInches() {
        return Conversions.ticksToInches(mLeftMaster.getEncoder().getPosition());
    }

    public double getRightInches() {
        return Conversions.ticksToInches(mRightMaster.getEncoder().getPosition());
    }

    public double getLeftVelTicks() {
        return mLeftMaster.getEncoder().getVelocity();
    }

    public double getRightVelTicks() {
        return mRightMaster.getEncoder().getVelocity();
    }

    /**
     * TODO Not available with current API
     * @return
     */
    public double getLeftTarget() {
        return 0.0;
    }

    /**
     * TODO Not available with current API
     * @return
     */
    public double getRightTarget() {
        return 0.0;
    }

    public double getLeftVelInches() {
        return Conversions.ticksPerTimeUnitToRadiansPerSecond(getLeftVelTicks());
    }

    public double getRightVelInches() {
        return Conversions.ticksPerTimeUnitToRadiansPerSecond(getRightVelTicks());
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
        return mLeftMaster.getAppliedOutput() * 12.0;
    }

    @Override
    public double getRightVoltage() {
        return mRightMaster.getAppliedOutput() * 12.0;
    }

    @Override
    public void setOpenLoopRampRate(double pOpenLoopRampRate) {
        mLeftMaster.setOpenLoopRampRate(pOpenLoopRampRate);
        mLeftMiddle.setOpenLoopRampRate(pOpenLoopRampRate);
        mLeftRear.setOpenLoopRampRate(pOpenLoopRampRate);

        mRightMaster.setOpenLoopRampRate(pOpenLoopRampRate);
        mRightMiddle.setOpenLoopRampRate(pOpenLoopRampRate);
        mRightRear.setOpenLoopRampRate(pOpenLoopRampRate);
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
