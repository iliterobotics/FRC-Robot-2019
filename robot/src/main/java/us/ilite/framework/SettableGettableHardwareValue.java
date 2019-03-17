package us.ilite.framework;

public class SettableGettableHardwareValue<T> implements IHardwareValue<T> {

    private final GettableHardwareValue<T> mGettableHardwareValue;
    private final SettableHardwareValue<T> mSettableHardwareValue;

    public SettableGettableHardwareValue(GettableHardwareValue.HardwareGetter<T> pGetter, SettableHardwareValue.HardwareSetter<T> pSetter, T pInitialGetValue, T pInitialSetValue) {
        mGettableHardwareValue = new GettableHardwareValue<>(pGetter, pInitialGetValue);
        mSettableHardwareValue = new SettableHardwareValue<>(pSetter, pInitialSetValue);
    }

    public SettableGettableHardwareValue(GettableHardwareValue.HardwareGetter<T> pGetter, SettableHardwareValue.HardwareSetter<T> pSetter) {
        this(pGetter, pSetter, null, null);
    }

    @Override
    public T get() {
        return mGettableHardwareValue.get();
    }

    @Override
    public void set(T value) {
        mSettableHardwareValue.set(value);
    }

    @Override
    public void update() {
        mSettableHardwareValue.update();
        mGettableHardwareValue.update();
    }

}
