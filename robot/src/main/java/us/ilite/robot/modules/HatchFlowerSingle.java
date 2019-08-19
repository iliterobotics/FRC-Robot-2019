package us.ilite.robot.modules;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Timer;
import us.ilite.common.Data;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.types.drive.EDriveData;
import us.ilite.robot.hardware.SolenoidWrapper;

public class HatchFlowerSingle extends Module{
    private static HatchFlowerSingle HatchFlowerInstance = new HatchFlowerSingle();

    private ILog mLog = Logger.createLog(HatchFlowerSingle.class);

    private Data mData;

    private Timer mBackupTimer = new Timer();
    private Solenoid mGrab;
    private Solenoid mExtend;
    private SolenoidWrapper mGrabSolenoid;
    private SolenoidWrapper mExtendSolenoid;

    private HatchFlowerSingle.GrabberState mLastGrabberState, mGrabberState;
    private HatchFlowerSingle.ExtensionState mExtensionState;

    private double mReleaseDistance = 0;

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

    public static HatchFlowerSingle getInstance() {
        return HatchFlowerInstance;
    }

    private HatchFlowerSingle() {
        mGrab = new Solenoid( SystemSettings.kCANAddressPCM, SystemSettings.kHatchFlowerOpenCloseSolenoidAddress);
        mExtend = new Solenoid(SystemSettings.kCANAddressPCM, SystemSettings.kHatchFlowerExtensionSolenoidAddress);
        mGrabSolenoid = new SolenoidWrapper(mGrab);
        mExtendSolenoid = new SolenoidWrapper(mExtend);

        // Init Hatch Flower to grab state - Per JKnight we will start with a hatch or cargo onboard
        this.mLastGrabberState = HatchFlowerSingle.GrabberState.GRAB;
        this.mGrabberState = HatchFlowerSingle.GrabberState.GRAB;
        this.mExtensionState = HatchFlowerSingle.ExtensionState.UP;

        this.mGrabSolenoid.set( HatchFlowerSingle.GrabberState.GRAB.grabber);
        this.mExtendSolenoid.set( HatchFlowerSingle.ExtensionState.UP.extension);
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

        mGrabSolenoid.set(mGrabberState.grabber);
        mExtendSolenoid.set(mExtensionState.extension);

        if(hasGrabberStateChanged() && mGrabberState == HatchFlowerSingle.GrabberState.RELEASE && mExtensionState == HatchFlowerSingle.ExtensionState.DOWN) {
            mReleaseDistance = getAverageDistanceTraveled();
            mBackupTimer.reset();
            mBackupTimer.start();
        }

        mLastGrabberState = mGrabberState;
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
        mGrabberState = HatchFlowerSingle.GrabberState.GRAB;
    }

    /**
     * Configure the solenoids to push the hatch
     */
    public void pushHatch() {
        mGrabberState = HatchFlowerSingle.GrabberState.RELEASE;
    }

    public boolean shouldBackUp() {

        double distanceDelta = mReleaseDistance - getAverageDistanceTraveled();

        return
                mGrabberState == HatchFlowerSingle.GrabberState.RELEASE &&
                        mExtensionState == HatchFlowerSingle.ExtensionState.DOWN &&
                        distanceDelta <= SystemSettings.kHatchFlowerReleaseDistance &&
                        distanceDelta > 0;

//        return mBackupTimer.get() <= SystemSettings.kHatchFlowerReleaseTime;
    }

    public boolean hasGrabberStateChanged() {
        return mLastGrabberState != mGrabberState;
    }

    private double getAverageDistanceTraveled() {
        return (mData.drive.get( EDriveData.LEFT_POS_INCHES) + mData.drive.get(EDriveData.LEFT_POS_INCHES)) / 2.0;
    }

    /**
     * Sets whether hatch grabber is up or down
     * @param pExtensionState
     */
    public void setFlowerExtended( HatchFlowerSingle.ExtensionState pExtensionState) {
        mExtensionState = pExtensionState;
    }

    public HatchFlowerSingle.ExtensionState getExtensionState() {
        return mExtensionState;
    }

    public HatchFlowerSingle.GrabberState getGrabberState() {
        return mGrabberState;
    }

    /**
     * This should be dictated by sensor value if possible.
     * @return Whether the hatch grabber is currently holding a hatch.
     */
    public boolean hasHatch() {
        return false;
    }

    public void setData( Data pData) {
        this.mData = pData;
    }
}
