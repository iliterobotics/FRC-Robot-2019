package us.ilite.common.types.auton;

public enum EHatchRocketAction {
    FRONT,
    BACK,
    NONE;

    public static EHatchRocketAction intToEnum( int num ) {
        return values()[num];
    }
}
