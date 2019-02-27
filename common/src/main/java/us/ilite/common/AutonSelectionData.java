package us.ilite.common;

import us.ilite.common.types.auton.EStartingPosition;

public class AutonSelectionData {

    public final ECargoAction mCargoAction;
    public final EHatchAction mHatchAction;
    public final EStartingPosition mStartingPosition;

    public AutonSelectionData(ECargoAction pCargoAction, EHatchAction pHatchAction, EStartingPosition pStartingPosition) {
        mCargoAction = pCargoAction;
        mHatchAction = pHatchAction;
        mStartingPosition = pStartingPosition;
    }
}
