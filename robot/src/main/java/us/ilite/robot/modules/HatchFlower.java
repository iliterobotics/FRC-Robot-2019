package us.ilite.robot.modules;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Timer;
import us.ilite.common.config.SystemSettings;

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

    private Solenoid mGrabSolenoid;
    private Solenoid mExtendSolenoid;
    private DigitalInput mUpperHatchSwitch, mLowerHatchSwitch;

    private GrabberState mGrabberState;
    private ExtensionState mExtensionState;
    private Timer mHasHatchTimer = new Timer();
    private boolean mHasHatch = false;


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
        mGrabSolenoid = new Solenoid(SystemSettings.kCANAddressPCM, SystemSettings.kHatchFlowerOpenCloseSolenoidAddress);
        mExtendSolenoid = new Solenoid(SystemSettings.kCANAddressPCM, SystemSettings.kHatchFlowerExtensionSolenoidAddress);
//        mUpperHatchSwitch = new DigitalInput();
//        mLowerHatchSwitch = new DigitalInput();

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

        if(isHatchSwitchTriggered()) {
            mHasHatchTimer.reset();
            mHasHatchTimer.start();
        } else {
            mHasHatchTimer.stop();
        }

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
        return isHatchSwitchTriggered() && mHasHatchTimer.hasPeriodPassed(SystemSettings.kHatchFlowerSwitchPressedTime);
    }

    private boolean isHatchSwitchTriggered() {
        return mLowerHatchSwitch.get() || mUpperHatchSwitch.get();
    }

}
