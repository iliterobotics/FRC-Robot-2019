package us.ilite.framework;

public class GettableHardwareValue<T> implements IHardwareValue<T> {

    private final HardwareGetter<T> mHardwareGetter;
    private T mValue;

    public interface HardwareGetter<V> {
        V get();
    }

    public GettableHardwareValue(HardwareGetter<T> pHardwareGetter, T pValue) {
        mHardwareGetter = pHardwareGetter;
        mValue = pValue;
    }

    public GettableHardwareValue(HardwareGetter<T> pHardwareGetter) {
        this(pHardwareGetter, null);
    }

    @Override
    public T get() {
        return mValue;
    }

    @Override
    public void set(T value) {
        mValue = value;
    }

    @Override
    public void update() {
        mValue = mHardwareGetter.get();
    }

}
