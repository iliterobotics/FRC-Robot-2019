package us.ilite.common.types.input;


public enum EDriverInputMode {
	ARCADE,
	SPLIT_ARCADE;
	public static EDriverInputMode intToEnum(int num) {
		if(num <= -1)return ARCADE;
		return values()[num];
	}
}
