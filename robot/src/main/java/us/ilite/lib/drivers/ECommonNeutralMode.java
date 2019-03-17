package us.ilite.lib.drivers;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.revrobotics.CANSparkMax;

public enum ECommonNeutralMode {

    BRAKE(NeutralMode.Brake, CANSparkMax.IdleMode.kBrake),
    COAST(NeutralMode.Coast, CANSparkMax.IdleMode.kCoast);

    public final NeutralMode kCtreNeutralMode;
    public final CANSparkMax.IdleMode kRevIdleMode;

    ECommonNeutralMode(NeutralMode pKCtreNeutralMode, CANSparkMax.IdleMode pKRevIdleMode) {
        kCtreNeutralMode = pKCtreNeutralMode;
        kRevIdleMode = pKRevIdleMode;
    }

}
