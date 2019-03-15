package us.ilite.common.types.auton;

public enum ECargoRocketAction {
    MID;

    public static ECargoRocketAction intToEnum( int num ) {
        return values()[num];
    }
}
