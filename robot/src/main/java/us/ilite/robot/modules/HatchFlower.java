package us.ilite.robot.modules;

import java.util.Arrays;
import java.util.List;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Timer;
import us.ilite.common.config.SystemSettings;
import us.ilite.robot.commands.CommandQueue;
import us.ilite.robot.commands.Delay;
import us.ilite.robot.commands.ParallelCommand;
import us.ilite.robot.commands.ICommand;

/**
 * Control the Hatch Flower
 *
 * The Hatch Capture button will take precedence over the Hatch Push button.
 * Once the Hatch Capture button is pressed, the hatch flower will transition to
 * the CAPTURE state and stay there until the Hatch Push button is pressed.
 * Then the Hatch Flower will push the hatch and go to the RELEASE configuration.
 */
public class HatchFlower extends Module {

    public enum HatchFlowerState {
        CAPTURE,
        PUSH,
        RELEASE;
    }

    private ILog mLog = Logger.createLog(HatchFlower.class);

    private Solenoid grabSolenoid;
    private Solenoid pushSolenoid;
    private Solenoid extendSolenoid;

    // Needed for prototype testing
    // private final Codex<Double, ELogitech310> mController;

    private CommandQueue mCurrentCommandQueue;

    private HatchFlowerState mCurrentState;
    private ExtensionState mExtensionState;


    /////////////////////////////////////////////////////////////////////////
    // ********************** Solenoid state enums *********************** //
    public enum GrabberState
    {
        GRAB(true),  // set to solenoid state that corresponds with grabbing the hatch
        RELEASE(false);

        private boolean grabber;

        private GrabberState(boolean grabber)
        {
            this.grabber = grabber;
        }
    }

    public enum PusherState
    {
        PUSH(true), // set to the solenoid state that corresponds with pushing the hatch
        RESET(false);

        private boolean pusher;

        private PusherState(boolean pusher)
        {
            this.pusher = pusher;
        }
    }

    public enum ExtensionState {
        UP(true),
        DOWN(false);

        private boolean extension;

        ExtensionState(boolean pExtension) {
            extension = pExtension;
        }
    }
    /////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////


    /////////////////////////////////////////////////////////////////////////
    // ********************* Solenoid Change Commands ******************** //

    /**
     * The GrabSolenoidCommand will set the grab solenoid to the commanded state,
     * which is provided in the constructor
     */
    public class GrabSolenoidCommand implements ICommand {

        GrabberState state;

        GrabSolenoidCommand(GrabberState state) {
            this.state = state;
        }

        @Override
        public void init(double pNow) {
            // Set the commanded solenoid state at init
            grabSolenoid.set(this.state.grabber);
        }

        @Override
        public boolean update(double pNow) {
            // always return done, delays handled by the Delay command
            return true;
        }

        @Override
        public void shutdown(double pNow) {

        }

    }

    /**
     * The PushSolenoidCommand will set the push solenoid to the commanded state,
     * which is provided in the constructor
     */
    public class PushSolenoidCommand implements ICommand {

        PusherState state;

        PushSolenoidCommand(PusherState state) {
            this.state = state;
        }

        @Override
        public void init(double pNow) {
            // Set the commanded solenoid state at init
            pushSolenoid.set(this.state.pusher);
        }

        @Override
        public boolean update(double pNow) {
            // always return done, delays handled by the Delay command
            return true;
        }

        @Override
        public void shutdown(double pNow) {

        }

    }

    public class ExtendSolenoidCommand implements ICommand {

        private ExtensionState state;
        private Timer timer = new Timer();

        public ExtendSolenoidCommand(ExtensionState pState) {
            state = pState;
        }

        @Override
        public void init(double pNow) {
            timer.reset();
            timer.start();
            extendSolenoid.set(state.extension);
        }

        @Override
        public boolean update(double pNow) {
            if(timer.hasPeriodPassed(SystemSettings.kHatchFlowerExtendStatusTimerDuration)) {
                // Update state to current
                mExtensionState = state;
                return true;
            }
            return false;
        }

        @Override
        public void shutdown(double pNow) {

        }

    }

    /**
     * SetStateCommand will set the HatchFlower state.  This is needed because the Push
     * command is transitional.
     */
    public class SetStateCommand implements ICommand {

        HatchFlowerState mState;

        SetStateCommand( HatchFlowerState state) {
            this.mState = state;
        }
        @Override
        public void init(double pNow) {
            HatchFlower.this.mCurrentState = this.mState;
        }

        @Override
        public boolean update(double pNow) {
            return true;
        }

        @Override
        public void shutdown(double pNow) {

        }

    }
    /////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////




    // Needed for prototype testing
    // public HatchFlower(Codex<Double, ELogitech310> pController) {
    public HatchFlower() {

        // Needed for prototype testing
        // The controller has the controller button states
        // this.mController = pController;

        // TODO Do we need to pass the CAN Addresses in via the constructor?
        grabSolenoid = new Solenoid(SystemSettings.kCANAddressPCM, SystemSettings.kHatchFlowerOpenCloseSolenoidAddress);
//        pushSolenoid = new Solenoid(SystemSettings.kCANAddressPCM, SystemSettings.kHatchFlowerPushSolenoidAddress);
        extendSolenoid = new Solenoid(SystemSettings.kCANAddressPCM, SystemSettings.kHatchFlowerExtensionSolenoidAddress);
//        grabSolenoid = new Solenoid(4); //Ball Grabber/Holder (2016 robot)
////        pushSolenoid = new Solenoid(3); //Ball Kicker (2016 robot)

        // Init Hatch Flower to grab state - Per JKnight we will start with a hatch or cargo onboard
        this.mCurrentState = HatchFlowerState.CAPTURE;
        this.mExtensionState = ExtensionState.DOWN;

        this.grabSolenoid.set(GrabberState.GRAB.grabber);
//        this.pushSolenoid.set(PusherState.RESET.pusher);
        this.extendSolenoid.set(ExtensionState.DOWN.extension);

        // Command queue to hold the solenoid transition commands
        mCurrentCommandQueue = new CommandQueue();


    }

    /**
     *
     */
    @Override
    public void modeInit(double pNow) {
        mLog.error("MODE INIT");
        mCurrentCommandQueue.init(pNow);
    }

    @Override
    public void periodicInput(double pNow) {

    }

    @Override
    public void update(double pNow) {

        // update any currently running commands first
        mCurrentCommandQueue.update(pNow);

        // // Needed for prototype testing
        // // TODO Do we need to worry about both buttons being pressed? We will set a
        // // button priority.  Maybe both pressed is a reset??
        // // Check to see if the Hatch Flower buttons are pressed.
        // if (this.mController.isSet(DriveTeamInputMap.DRIVER_HATCH_FLOWER_CAPTURE_BTN)) {
        //     this.captureHatch();
        // } else if (this.mController.isSet(DriveTeamInputMap.DRIVER_HATCH_FLOWER_PUSH_BTN)) {
        //     // Only trigger push if the capture button is NOT being pushed
        //     this.pushHatch();
        // }

    }

    @Override
    public void shutdown(double pNow) {
        mCurrentCommandQueue.shutdown(pNow);
    }

    @Override
    public boolean checkModule(double pNow) {
        return true; // TODO What does this do?
    }

    /**
     * Configure the solenoids to capture a hatch
     */
    public void captureHatch() {
        // Handle logic to capture the hatch
        // First assumption:  Existing commands must be complete before starting this command
        if (mCurrentCommandQueue.isDone()) {
            // Command Queue is empty, so we can load our capture commands

            // check the current state
            switch(this.mCurrentState) {
                case CAPTURE :
                    // Allow reissue of the CAPTURE command in case it's needed,
                    // The solenoids should already be in the required state, so no harm.
                case RELEASE :
                    // We must be in the RELEASE state before we can capture:

                    // Step1: set the end state
                    SetStateCommand endState = new SetStateCommand(HatchFlowerState.CAPTURE);

                    // Step2: Start the solenoids moving to the capture configuration
                    List<ICommand> toCaptureState = Arrays.asList(
                            new GrabSolenoidCommand(GrabberState.GRAB),
                            /*new PushSolenoidCommand(PusherState.RESET),*/
                            new Delay(SystemSettings.kHatchFlowerSolenoidReleaseTimeSec)
                    );

                    ParallelCommand step2= new ParallelCommand(toCaptureState);

                    mCurrentCommandQueue.setCommands(endState, step2);

                    break;

                case PUSH :
                    // We must complete the Push before we can capture, do nothing
                    break;

                default :
                    // We should not be here, log the error
                    mLog.warn("HatchFlower.captureHatch: Unknown state");
                    break;

            }
        }
    }

    /**
     * Configure the solenoids to push the hatch
     */
    public void pushHatch() {
        // Handle logic to push the hatch
        // First assumption:  Existing commands must be complete before starting this command
        if (mCurrentCommandQueue.isDone()) {
            // Command Queue is empty, so we can load our push commands

            // check the current state
            switch(this.mCurrentState) {
                case RELEASE :
                case CAPTURE :
                    // We must be in the CAPTURE or RELEASE states before we can PUSH:

                    //// STEP 1 /////
                    // Step1: set the start state
                    SetStateCommand initialState = new SetStateCommand(HatchFlowerState.PUSH);

                    //// STEP 2 /////
                    // Step2: Start pushing the hatch
                    ParallelCommand pushState = new ParallelCommand(
                            new GrabSolenoidCommand(GrabberState.GRAB),
                            /*new PushSolenoidCommand(PusherState.PUSH),*/
                            // The delay between start of push and release of grab
                            new Delay(SystemSettings.kHatchFlowerGrabToPushTransitionTimeSec)
                    );

                    //// STEP 3 /////
                    // Step3: Start releasing the grabber
                    ParallelCommand releaseGrabberState = new ParallelCommand(
                            new GrabSolenoidCommand(GrabberState.RELEASE),
                            /*new PushSolenoidCommand(PusherState.PUSH),*/
                            // The time to leave pusher out (Solenoid settle + push time)
                            new Delay(SystemSettings.kHatchFlowerPushDurationSec)
                    );

                    //// STEP 4 /////
                    // Step4: Set Start reseting the push
                    ParallelCommand resetPushState = new ParallelCommand(
                            new GrabSolenoidCommand(GrabberState.RELEASE),
                            new PushSolenoidCommand(PusherState.RESET),
                            // Time to allow the solenoid to reset
                            new Delay(SystemSettings.kHatchFlowerSolenoidReleaseTimeSec)
                    );

                    //// STEP 5 /////
                    // Step5: set the end state
                    SetStateCommand endState = new SetStateCommand(HatchFlowerState.RELEASE);

                    // start the push command running
                    mCurrentCommandQueue.setCommands(initialState, pushState, releaseGrabberState, resetPushState, endState);

                case PUSH :
                    // We must complete the Push before we can Push again, do nothing
                    break;

                default :
                    // We should not be here, log the error
                    mLog.warn("HatchFlower.pushHatch: Unknown state");
                    break;

            }
        }

    }

    /**
     * Sets whether hatch grabber is up or down
     * @param pExtensionState
     */
    public void setFlowerExtended(ExtensionState pExtensionState) {
        if(mCurrentCommandQueue.isDone()) {
            mCurrentCommandQueue.setCommands(
                    new ExtendSolenoidCommand(pExtensionState)
            );
        }
    }

    public ExtensionState getExtensionState() {
        return mExtensionState;
    }

    public HatchFlowerState getHatchFlowerState() {
        return mCurrentState;
    }

    /**
     * This should be dictated by sensor value if possible.
     * @return Whether the hatch grabber is currently holding a hatch.
     */
    public boolean hasHatch() {
        return true;
    }

}
