package us.ilite.common.types.manipulator;

import com.flybotix.hfr.codex.Codex;
import com.flybotix.hfr.codex.CodexOf;

public enum EElevator implements CodexOf<Double> {

    DESIRED_POWER,
    OUTPUT_POWER,
    DESIRED_ENCODER_TICKS,
    CURRENT_ENCODER_TICKS,
    CURRENT,
    BUS_VOLTAGE,
    DESIRED_POSITION_TYPE,
    CURRENT_STATE

}

