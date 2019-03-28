package us.ilite.common.types.auton;

public enum EHatchShipAction {
    FRONT,
    LEFT,
    RIGHT;

    public static EHatchShipAction intToEnum( int num ) {
        return values()[num];
    }
}