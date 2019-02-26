package us.ilite.common.types.manipulator;

import com.flybotix.hfr.codex.Codex;
import com.flybotix.hfr.codex.CodexOf;

public enum EElevator implements CodexOf<Double> {

    DESIRED_POWER,
    OUTPUT_POWER,
    DESIRED_ENCODER_TICKS,
    CURRENT_ENCODER_TICKS,
    CURRENT_NEO_TICKS,
    CURRENT,
    BUS_VOLTAGE,
    DESIRED_POSITION,
    CURRENT_POSITION,
    CURRENT_STATE,
    AT_TOP,
    AT_BOTTOM,
    DESIRED_DIRECTION_UP,
    DESIRED_POSITION_ABOVE_INITIAL,
    SETTING_POSITION,
    CURRENT_CONTROL_MODE

}

