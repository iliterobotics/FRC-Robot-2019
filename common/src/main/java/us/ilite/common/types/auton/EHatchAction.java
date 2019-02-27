package us.ilite.common.types.auton;

public enum EHatchAction{
    A,B,C;

    public static EHatchAction intToEnum(int num) {
        return values()[num];
    }
}