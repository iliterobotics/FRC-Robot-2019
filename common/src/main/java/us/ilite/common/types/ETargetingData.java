package us.ilite.common.types;

import com.flybotix.hfr.codex.CodexOf;

public enum ETargetingData implements CodexOf<Double> {
    // TODO - document what these values represent
    tv,
    tx,
    ty,
    ta,
    ts,
    tl,
    tshort,
    tlong,
    thoriz,
    tvert,
    
    targetOrdinal,
    calcDistToTarget,
    calcAngleToTarget,
    calcTargetX,
    calcTargetY
}