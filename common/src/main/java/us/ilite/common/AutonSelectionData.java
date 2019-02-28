package us.ilite.common;

import us.ilite.common.types.auton.ECargoShipAction;
import us.ilite.common.types.auton.ECargoRocketAction;
import us.ilite.common.types.auton.EHatchRocketAction;
import us.ilite.common.types.auton.EHatchShipAction;
import us.ilite.common.types.auton.EStartingPosition;

public class AutonSelectionData {

    public final ECargoRocketAction mCargoRocketAction;
    public final ECargoShipAction mCargoShipAction;
    public final EHatchRocketAction mHatchRocketAction;
    public final EHatchShipAction mHatchShipAction;
    public final EStartingPosition mStartingPosition;

    public AutonSelectionData(ECargoRocketAction pCargoRocketAction, ECargoShipAction pCargoShipAction, EHatchRocketAction pHatchRocketAction, EHatchShipAction pHatchShipAction, EStartingPosition pStartingPosition) {
        this.mCargoRocketAction = pCargoRocketAction;
        this.mCargoShipAction = pCargoShipAction;
        this.mHatchRocketAction = pHatchRocketAction;
        this.mHatchShipAction = pHatchShipAction;
        this.mStartingPosition = pStartingPosition;
    }
}
