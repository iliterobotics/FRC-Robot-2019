package us.ilite.common.types.auton;

public enum EHatchRocketAction {
    LEFT,
    RIGHT;

    public static EHatchRocketAction intToEnum( int num ) {
        return values()[num];
    }
}
