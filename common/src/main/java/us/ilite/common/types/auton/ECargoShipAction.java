package us.ilite.common.types.auton;

public enum ECargoShipAction {
    FRONT_LEFT,
    FRONT_RIGHT;

    public static ECargoShipAction intToEnum( int num) {
        if(num >= values().length || num < 0) return FRONT_LEFT;
        return values()[num];
    }

}