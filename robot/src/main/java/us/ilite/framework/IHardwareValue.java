package us.ilite.framework;

public interface IHardwareValue<T> {

    T get();
    void set(T value);
    void update();

}
