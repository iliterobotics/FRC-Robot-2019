package us.ilite.robot.hardware;

/**
 * Instead of a central Hardware class, like we used last year, we're separating hardware by Module.
 * The idea behind this is threefold:
 * 1. Reduce the size (and constructor boilerplate) and complexity of Modules,
 * 2. Maintain something approaching best practices while still using dependency injection,
 * 3. Provide a high level way to simulate robot hardware (i.e. NOT making passthrough classes for every part of WPILib/CTRE)
 */
public interface IHardware {

    void init();
    void zero();
    boolean checkHardware();

}
