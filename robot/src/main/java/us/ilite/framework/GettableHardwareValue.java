package us.ilite.framework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class GettableHardwareValue<T> implements IHardwareValue<T> {

    private final HardwareGetter<T> mHardwareGetter;
    private T mValue;

    public interface HardwareGetter<V> {
        V get();
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface GettableHardware{

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
