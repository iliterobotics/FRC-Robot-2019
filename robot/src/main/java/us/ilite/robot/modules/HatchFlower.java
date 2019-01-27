package us.ilite.robot.modules;

import java.util.Arrays;
import java.util.List;

import com.flybotix.hfr.codex.Codex;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

import edu.wpi.first.wpilibj.Solenoid;
import us.ilite.common.config.DriveTeamInputMap;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.types.input.ELogitech310;
import us.ilite.robot.commands.CommandQueue;
import us.ilite.robot.commands.Delay;
import us.ilite.robot.commands.ICommand;
import us.ilite.robot.commands.ParallelCommand;

/**
 * Control the Hatch Flower
 * 
 * The Hatch Capture button will take precedence over the Hatch Push button.
 * Once the Hatch Capture button is pressed, the hatch flower will transition to
 * the CAPTURE state and stay there until the Hatch Push button is pressed.
 * Then the Hatch Flower will push the hatch and go to the RELEASE configuration.
 */
public class HatchFlower extends Module {

    public enum HatchFlowerStates {
        CAPTURE, 
        PUSH,
        RELEASE;
    }

    private ILog mLog = Logger.createLog(HatchFlower.class);

    private Solenoid solenoidOpenClose;
    private Solenoid solenoidExtension;

    // Needed for prototype testing
    // private final Codex<Double, ELogitech310> mController;

    private CommandQueue mCurrentCommandQueue;

    private HatchFlowerStates mCurrentState;

    
    /////////////////////////////////////////////////////////////////////////
    // ********************** Solenoid state enums *********************** //
    public enum GrabberState
    { 
      GRAB(true),
      RELEASE(false);
  
      private boolean grabber;
  
      private GrabberState(boolean grabber)
      {
        this.grabber = grabber;
      }
    }
  
    public enum PusherState
    {
      PUSH(true),
      RESET(false);
  
      private boolean pusher;
  
      private PusherState(boolean pusher)
      {
        this.pusher = pusher;
      }
    }
    /////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////


    /////////////////////////////////////////////////////////////////////////
    // ********************* Solenoid Change Commands ******************** //

    public class GrabSolenoidCommand implements ICommand {

        GrabberState state;

        GrabSolenoidCommand(GrabberState state) {
            this.state = state;
        }

        @Override
        public void init(double pNow) {
            solenoidOpenClose.set(this.state.grabber);
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

    public class PushSolenoidCommand implements ICommand {

        PusherState state;

        PushSolenoidCommand(PusherState state) {
            this.state = state;
        }

        @Override
        public void init(double pNow) {
            solenoidExtension.set(this.state.pusher);
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
     * SetStateCommand will set the HatchFlower state.  This is needed because the Push
     * command is transitional.
     */
    public class SetStateCommand implements ICommand {

        HatchFlowerStates mState;

        SetStateCommand( HatchFlowerStates state) {
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

        this.mCurrentState = HatchFlowerStates.RELEASE;

        // TODO Do we need to pass the CAN Addresses in via the constructor?
        solenoidOpenClose = new Solenoid(SystemSettings.kHatchFlowerOpenCloseSolenoidAddress);
        solenoidExtension = new Solenoid(SystemSettings.kHatchFlowerExtensionSolenoidAddress);

        // Init Hatch Flower to release state
        solenoidOpenClose.set(GrabberState.RELEASE.grabber);
        solenoidExtension.set(PusherState.RESET.pusher);

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
                case RELEASE :
                    // We must be in the RELEASE state before we can capture:

                    // Step1: set the end state
                    SetStateCommand step1 = new SetStateCommand(HatchFlowerStates.CAPTURE);

                    // Step2: Start the solenoids moving to the capture configuration
                    List<ICommand> step2List = Arrays.asList(
                        new GrabSolenoidCommand(GrabberState.GRAB), 
                        new PushSolenoidCommand(PusherState.RESET), 
                        new Delay(SystemSettings.kHatchFlowerSolenoidReleaseTimeSec)
                    );

                    ParallelCommand step2= new ParallelCommand(step2List);

                    mCurrentCommandQueue.setCommands(step1, step2);

                break;

                case CAPTURE :
                    // we are already in capture state, do nothing
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
                    SetStateCommand step1 = new SetStateCommand(HatchFlowerStates.PUSH);

                    //// STEP 2 /////
                    // Step2: Start pushing the hatch
                    List<ICommand> step2List = Arrays.asList(
                        new GrabSolenoidCommand(GrabberState.GRAB), 
                        new PushSolenoidCommand(PusherState.PUSH), 
                        // The delay between start of push and release of grab
                        new Delay(SystemSettings.kHatchFlowerGrabToPushTransitionTimeSec) 
                    );

                    ParallelCommand step2 = new ParallelCommand(step2List);

                    //// STEP 3 /////
                    // Step3: Start releasing the grabber
                    List<ICommand> step3List = Arrays.asList(
                        new GrabSolenoidCommand(GrabberState.RELEASE), 
                        new PushSolenoidCommand(PusherState.PUSH), 
                        // The time to leave pusher out (Solenoid settle + push time)
                        new Delay(SystemSettings.kHatchFlowerPushDurationSec) 
                    );

                    ParallelCommand step3 = new ParallelCommand(step3List);

                    //// STEP 4 /////
                    // Step4: Set Start reseting the push
                    List<ICommand> step4List = Arrays.asList(
                        new GrabSolenoidCommand(GrabberState.RELEASE), 
                        new PushSolenoidCommand(PusherState.RESET), 
                        // Time to allow the solenoid to reset
                        new Delay(SystemSettings.kHatchFlowerSolenoidReleaseTimeSec)
                    );

                    ParallelCommand step4 = new ParallelCommand(step4List);

                    //// STEP 5 /////
                    // Step5: set the end state
                    SetStateCommand step5 = new SetStateCommand(HatchFlowerStates.RELEASE);

                    // start the push command running
                    mCurrentCommandQueue.setCommands(step1, step2, step3, step4, step5);

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

}
