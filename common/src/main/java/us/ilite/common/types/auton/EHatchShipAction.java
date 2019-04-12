package us.ilite.common.types.auton;

public enum EHatchShipAction {
    FRONT_LEFT,
    FRONT_RIGHT,
    NONE;

    public static EHatchShipAction intToEnum( int num ) {
        return values()[num];
    }
}