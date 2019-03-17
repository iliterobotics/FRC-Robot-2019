package us.ilite.framework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class SettableGettableHardwareValue<T> implements IHardwareValue<T> {

    private final GettableHardwareValue<T> mGettableHardwareValue;
    private final SettableHardwareValue<T> mSettableHardwareValue;

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface GettableSettableHardware {
    }

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
