package us.ilite.common.types.auton;

public enum ECargoRocketAction {
    FRONT,
    LEFT,
    RIGHT,
    NONE;

    public static ECargoRocketAction intToEnum( int num ) {
        return values()[num];
    }
}
