package us.ilite.common.types;

import com.flybotix.hfr.codex.CodexOf;

public enum EFourBarData implements CodexOf<Double>{
    STATE, ANGLE,
    A_TICKS, B_TICKS,
    A_OUTPUT, A_VOLTAGE, A_CURRENT,
    B_OUTPUT, B_VOLTAGE, B_CURRENT;
}