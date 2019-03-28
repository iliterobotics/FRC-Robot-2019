package us.ilite.robot.modules;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

import edu.wpi.first.wpilibj.Solenoid;
import us.ilite.common.config.SystemSettings;
import us.ilite.robot.hardware.SolenoidWrapper;

/**
 * Control the Hatch Flower
 *
 * The Hatch Capture button will take precedence over the Hatch Push button.
 * Once the Hatch Capture button is pressed, the hatch flower will transition to
 * the CAPTURE state and stay there until the Hatch Push button is pressed.
 * Then the Hatch Flower will push the hatch and go to the RELEASE configuration.
 */
public class HatchFlower extends Module {

    private ILog mLog = Logger.createLog(HatchFlower.class);

    private Solenoid mGrab;
    private Solenoid mExtend;
    private SolenoidWrapper mGrabSolenoid;
    private SolenoidWrapper mExtendSolenoid;

    private GrabberState mGrabberState;
    private ExtensionState mExtensionState;


    /////////////////////////////////////////////////////////////////////////
    // ********************** Solenoid state enums *********************** //
    public enum GrabberState
    {
        GRAB(false),  // set to solenoid state that corresponds with grabbing the hatch
        RELEASE(true);

        private boolean grabber;

        private GrabberState(boolean grabber)
        {
            this.grabber = grabber;
        }
    }

    public enum ExtensionState {
        UP(false),
        DOWN(true);

        private boolean extension;

        ExtensionState(boolean pExtension) {
            extension = pExtension;
        }
    }

    public HatchFlower() {
        // TODO Do we need to pass the CAN Addresses in via the constructor?
        mGrab = new Solenoid(SystemSettings.kCANAddressPCM, SystemSettings.kHatchFlowerOpenCloseSolenoidAddress);
        mExtend = new Solenoid(SystemSettings.kCANAddressPCM, SystemSettings.kHatchFlowerExtensionSolenoidAddress);
        mGrabSolenoid = new SolenoidWrapper(mGrab);
        mExtendSolenoid = new SolenoidWrapper(mExtend);

        // Init Hatch Flower to grab state - Per JKnight we will start with a hatch or cargo onboard
        this.mGrabberState = GrabberState.GRAB;
        this.mExtensionState = ExtensionState.DOWN;

        this.mGrabSolenoid.set(GrabberState.GRAB.grabber);
        this.mExtendSolenoid.set(ExtensionState.DOWN.extension);

    }

    /**
     *
     */
    @Override
    public void modeInit(double pNow) {
        mLog.error("MODE INIT");
    }

    @Override
    public void periodicInput(double pNow) {

    }

    @Override
    public void update(double pNow) {

        mGrabSolenoid.set(mGrabberState.grabber);
        mExtendSolenoid.set(mExtensionState.extension);

    }

    @Override
    public void shutdown(double pNow) {
    }

    @Override
    public boolean checkModule(double pNow) {
        return true; // TODO What does this do?
    }

    /**
     * Configure the solenoids to capture a hatch
     */
    public void captureHatch() {
        mGrabberState = GrabberState.GRAB;
    }

    /**
     * Configure the solenoids to push the hatch
     */
    public void pushHatch() {
        mGrabberState = GrabberState.RELEASE;
    }

    /**
     * Sets whether hatch grabber is up or down
     * @param pExtensionState
     */
    public void setFlowerExtended(ExtensionState pExtensionState) {
        mExtensionState = pExtensionState;
    }

    public ExtensionState getExtensionState() {
        return mExtensionState;
    }

    public GrabberState getGrabberState() {
        return mGrabberState;
    }

    /**
     * This should be dictated by sensor value if possible.
     * @return Whether the hatch grabber is currently holding a hatch.
     */
    public boolean hasHatch() {
        return true;
    }

}
