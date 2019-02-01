package us.ilite.common.types;

import com.flybotix.hfr.codex.CodexOf;

public enum EFourBarData implements CodexOf<Double>{
    STATE,
    A_OUTPUT,
    B_OUTPUT,
    A_VOLTAGE,
    B_VOLTAGE,
    ANGLE;
}