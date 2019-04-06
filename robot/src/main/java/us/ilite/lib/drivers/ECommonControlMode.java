package us.ilite.lib.drivers;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.revrobotics.ControlType;

/**
 * Provides a wrapper for the REV and CTRE API motor control modes so that we can use them interchangeably.
 */
public enum ECommonControlMode {

    VELOCITY(ControlMode.Velocity, ControlType.kVelocity),
    PERCENT_OUTPUT(ControlMode.PercentOutput, ControlType.kDutyCycle),
    CURRENT(ControlMode.Current, ControlType.kCurrent),
    POSITION(ControlMode.Position, ControlType.kPosition),
    MOTION_MAGIC(ControlMode.MotionMagic, ControlType.kSmartMotion);

    public final ControlMode kCtreControlMode;
    public final ControlType kRevControlType;

    ECommonControlMode(ControlMode pKCtreControlMode, ControlType pKRevControlType) {
        kCtreControlMode = pKCtreControlMode;
        kRevControlType = pKRevControlType;
    }


}
