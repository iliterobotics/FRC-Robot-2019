package us.ilite.frc.common.types;

public enum EStartingPosition {
    MIDDLE,
    BOTTOM,
	UNKNOWN;
  public static EStartingPosition intToEnum(int num) {
    if(num == -1)return UNKNOWN;
    if(num >= values().length || num < 0) return UNKNOWN;
    return values()[num];
  }
}
