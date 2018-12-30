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
     * Runs when we init a new robot mode, for example teleopInit() or autonomousInit()
     * @param pNow The current time from the FPGA
     */
    public abstract void modeInit(double pNow);

    /**
     * Design pattern for caching inputs to avoid hammering HAL/CAN.
     * Depending on whether you're using the Codex or not, this may or may not be necessary.
     */
    public abstract void periodicInput(double pNow);

    /**
     * The module's update function. Runs every time [mode]Periodic() is called (Roughly ~50Hz), or in a loop running at a custom frequency.
     * @param pNow
     */
    public abstract void update(double pNow);

    /**
     * Optional design pattern to keep hardware outputs all in one place.
     */
    public void periodicOutput(double pNow) {

    }

    /**
     * Shutdown/Cleanup tasks are performed here.
     * @param pNow
     */
    public abstract void shutdown(double pNow);

    /**
     * Runs a self-test routine on this module's hardware.
     */
    public boolean checkModule(double pNow) {
        return true;
    }

    /**
     * Zeroes sensors.
     */
    public void zeroSensors(double pNow) {
    }

}
