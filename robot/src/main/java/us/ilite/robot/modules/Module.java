package us.ilite.robot.modules;

/**
 * The Module class defines how code written to control a specific subsystem (shooter, elevator, arm, etc.).
 * It also contains optional design patterns to adhere to.
 * All methods are passed a time, which is expected to be consistent between all modules updated in the same [mode]Periodic() call.
 */
public abstract class Module {

    /*
    Although the Clock class removes the need for the now parameter, we will keep it since it may be useful to have
    in order to simulate certain conditions or edge cases.
     */

    /**
     * This runs once when the robot is powered on. It is intended for performing ROBOT initialization, NOT variable initialization.
     * Variable initialization should go in the class body or in the constructor - this is the best way to avoid accidental null pointers.
     * @param pNow The current time from the FPGA.
     */
    public abstract void powerOnInit(double pNow);

    /**
     * Runs when we init a new robot mode, for example teleopInit() or autonomousInit()
     * @param pNow The current time from the FPGA
     */
    public abstract void modeInit(double pNow);

    /**
     * The module's update function. Runs every time [mode]Periodic() is called (Roughly ~50Hz), or in a loop running at a custom frequency.
     * @param pNow
     */
    public abstract void update(double pNow);

    /**
     * Shutdown/Cleanup tasks are performed here.
     * @param pNow
     */
    public abstract void shutdown(double pNow);
    

    /**
     * Optional design pattern for caching inputs to avoid hammering HAL/CAN.
     */
    public void mapInputs(double pNow) {
    }
    
    /**
     * Runs a self-test routine on this module's hardware.
     */
    public abstract void checkModule(double pNow);

    /**
     * Zeroes sensors.
     */
    public void zeroSensors(double pNow) {
    }

}
