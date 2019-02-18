package us.ilite.common.types.auton;

public enum ECargoAction {
    A,
    B,
    C;

    public static ECargoAction intToEnum(int num) {
        if(num >= values().length || num < 0) return A;
        return values()[num];
    }

}