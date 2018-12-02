package us.ilite.common.types.drive;

import com.flybotix.hfr.codex.CodexOf;

public enum EDriveData implements CodexOf<Double> {

    LEFT_OUTPUT, RIGHT_OUTPUT,
    LEFT_POS_INCHES, RIGHT_POS_INCHES,
    LEFT_VEL_IPS, RIGHT_VEL_IPS,
    LEFT_CURRENT, RIGHT_CURRENT,
    LEFT_VOLTAGE, RIGHT_VOLTAGE,
    LEFT_CONTROLMODE, RIGHT_CONTROLMODE;

}
