package us.ilite.robot.modules;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

import edu.wpi.first.wpilibj.Solenoid;
import us.ilite.common.config.SystemSettings;


public class HatchFlower extends Module {

    private ILog mLog = Logger.createLog(HatchFlower.class);

    private Solenoid solenoidOpenClose;
    private Solenoid solenoidExtension;

    public enum SoleniodStates {
        // TODO hold solenoid states for each state
        CAPTURE,
        RELEASE,
        PUSH;
    }


    public HatchFlower() {
        // Construction
        // TODO Do we need to pass the CAN Addresses in via the constructor?  
        solenoidOpenClose = new Solenoid(SystemSettings.kHatchFlowerOpenCloseSolenoidAddress);
        solenoidExtension = new Solenoid(SystemSettings.kHatchFlowerExtensionSolenoidAddress);
        }


    @Override
    public void modeInit(double pNow) {
        mLog.error("MODE INIT");
    }

    @Override
    public void periodicInput(double pNow) {
        
    }

    @Override
    public void update(double pNow) {

    }

    @Override
    public void shutdown(double pNow) {
    }

    @Override
    public boolean checkModule(double pNow) {
        return false;
    }

    /**
     * Configure the solenoids to capture a hatch
     */
    public void captureHatch() {
        // TODO handle logic to capture the hatch
    }

    /**
     * Configure the solenoids to push the hatch
     */
    public void pushHatch() {
        // TODO handle logic to push the hatch

    }

    public void setFlowerExtended(boolean pFlowerExtended) {

    }

    /**
     *
     * @return Whether the hatch grabber is extended based on actuation time.
     */
    public boolean isExtended() {
        return true;
    }

    public boolean hasHatch() {
        return true;
    }

}
