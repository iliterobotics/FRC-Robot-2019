package us.ilite.common.types.manipulator;

import com.flybotix.hfr.codex.CodexOf;

public enum ECargoSpit implements CodexOf<Double> {

    INTAKING,
    OUTTAKING,
    STOPPED,
    HAS_CARGO,
    LEFT_CURRENT,
    RIGHT_CURRENT,
    BEAM_BROKEN
}
