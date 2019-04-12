package us.ilite.common.types.auton;

public enum EHatchRocketAction {
    LEFT,
    RIGHT,
    NONE;

    public static EHatchRocketAction intToEnum( int num ) {
        return values()[num];
    }
}
