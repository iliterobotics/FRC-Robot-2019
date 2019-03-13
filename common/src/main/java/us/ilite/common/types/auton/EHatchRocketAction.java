package us.ilite.common.types.auton;

public enum EHatchRocketAction {
    FRONT,
    LEFT,
    RIGHT;

    public static EHatchRocketAction intToEnum( int num ) {
        return values()[num];
    }
}
