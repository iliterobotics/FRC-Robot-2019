package us.ilite.common.types.auton;

public enum EStartingPosition {
    LEFT,
    RIGHT,
    MID_LEFT,
    MID_RIGHT,
	UNKNOWN;
  public static EStartingPosition intToEnum(int num) {
    if(num == -1)return UNKNOWN;
    if(num >= values().length || num < 0) return UNKNOWN;
    return values()[num];
  }
}