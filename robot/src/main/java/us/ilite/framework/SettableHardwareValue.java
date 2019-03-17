package us.ilite.framework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds a codex value to a function call
 */
public class SettableHardwareValue<T> implements IHardwareValue<T> {

    private final HardwareSetter<T> mHardwareSetter;
    private T mValue = null;

    public interface HardwareSetter<V> {
        void set(V value);
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SettableHardware{

    }

    public SettableHardwareValue(HardwareSetter<T> pHardwareSetter, T pValue) {
        mHardwareSetter = pHardwareSetter;
        mValue = pValue;
    }

    public SettableHardwareValue(HardwareSetter<T> pHardwareSetter) {
        this(pHardwareSetter, null);
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
        mHardwareSetter.set(mValue);
    }



}
