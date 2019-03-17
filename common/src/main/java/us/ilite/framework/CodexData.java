package us.ilite.framework;

public class CodexData {

    private final Double mValue;

    public CodexData(Double pValue) {
        mValue = pValue;
    }

    public static CodexData fromBoolean(boolean pValue) {
        return new CodexData(pValue ? 1.0 : 0.0);
    }

    public boolean toBoolean() {
        return mValue == 1.0;
    }

}
