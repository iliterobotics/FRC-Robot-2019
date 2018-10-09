package us.ilite.robot.hardware;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import us.ilite.common.config.SystemSettings;
import us.ilite.lib.drivers.TalonSRXFactory;

public class DriveHardware implements IHardware {

    private TalonSRX leftMaster, leftFollower, rightMaster, rightFollower;


    @Override
    public void init() {
        leftMaster = TalonSRXFactory.createDefaultTalon(0);
        leftFollower = TalonSRXFactory.createDefaultTalon(0);
        rightMaster = TalonSRXFactory.createDefaultTalon(0);
        rightFollower = TalonSRXFactory.createDefaultTalon(0);

        leftFollower.follow(leftMaster);
        rightFollower.follow(rightMaster);

        leftMaster.setStatusFramePeriod(StatusFrameEnhanced.Status_3_Quadrature, 10, SystemSettings.TALON_CONFIG_TIMEOUT_MS);
        rightMaster.setStatusFramePeriod(StatusFrameEnhanced.Status_3_Quadrature, 10, SystemSettings.TALON_CONFIG_TIMEOUT_MS);

        leftMaster.configOpenloopRamp(SystemSettings.DRIVE_RAMP_RATE, SystemSettings.TALON_CONFIG_TIMEOUT_MS);
        rightMaster.configOpenloopRamp(SystemSettings.DRIVE_RAMP_RATE, SystemSettings.TALON_CONFIG_TIMEOUT_MS);

        leftMaster.configContinuousCurrentLimit(SystemSettings.DRIVE_CONTINUOUS_CURRENT_LIMIT_AMPS, SystemSettings.TALON_CONFIG_TIMEOUT_MS);
        rightMaster.configContinuousCurrentLimit(SystemSettings.DRIVE_CONTINUOUS_CURRENT_LIMIT_AMPS, SystemSettings.TALON_CONFIG_TIMEOUT_MS);


        leftMaster.setInverted(false);
        leftFollower.setInverted(false);
        rightMaster.setInverted(true);
        rightFollower.setInverted(true);

        leftMaster.setSensorPhase(true);
        rightMaster.setSensorPhase(true);
    }

    @Override
    public void zero() {
        leftMaster.set(ControlMode.PercentOutput, 0.0);
        rightMaster.set(ControlMode.PercentOutput, 0.0);

        leftMaster.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, SystemSettings.TALON_CONFIG_TIMEOUT_MS);
        rightMaster.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, SystemSettings.TALON_CONFIG_TIMEOUT_MS);
    }

    public void configForVelocity(TalonSRX talon) {

    }

    public void configForPercentOutput(TalonSRX talon) {

    }

    public void configTalonForPosition(TalonSRX talon, int pidSlot, int errorTolerance, double p, double i, double d, double f) {
        talon.configAllowableClosedloopError(pidSlot, errorTolerance, SystemSettings.TALON_CONFIG_TIMEOUT_MS);
        talon.config_kP(pidSlot, p, SystemSettings.TALON_CONFIG_TIMEOUT_MS);
        talon.config_kI(pidSlot, i, SystemSettings.TALON_CONFIG_TIMEOUT_MS);
        talon.config_kD(pidSlot, d, SystemSettings.TALON_CONFIG_TIMEOUT_MS);
        talon.config_kF(pidSlot, f, SystemSettings.TALON_CONFIG_TIMEOUT_MS);
    }



    public void configTalonForMotionMagic(TalonSRX talon, int pidSlot, int loopSlot, double p, double i, double d, double f, int v, int a) {
        talon.selectProfileSlot(pidSlot, loopSlot);
        talon.config_kP(pidSlot, p, SystemSettings.TALON_CONFIG_TIMEOUT_MS);
        talon.config_kI(pidSlot, i, SystemSettings.TALON_CONFIG_TIMEOUT_MS);
        talon.config_kD(pidSlot, d, SystemSettings.TALON_CONFIG_TIMEOUT_MS);
        talon.config_kF(pidSlot, f, SystemSettings.TALON_CONFIG_TIMEOUT_MS);

        talon.configMotionCruiseVelocity(v, SystemSettings.TALON_CONFIG_TIMEOUT_MS);
        talon.configMotionAcceleration(a, SystemSettings.TALON_CONFIG_TIMEOUT_MS);

        talon.setSelectedSensorPosition(0, pidSlot, SystemSettings.TALON_CONFIG_TIMEOUT_MS);
    }

}
