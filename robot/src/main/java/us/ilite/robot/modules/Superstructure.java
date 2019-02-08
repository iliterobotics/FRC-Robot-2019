package us.ilite.robot.modules;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import us.ilite.robot.commands.CommandQueue;
import us.ilite.robot.commands.ICommand;

public class Superstructure extends Module {

    private ILog mLog = Logger.createLog(Superstructure.class);

    private CommandQueue mDesiredCommandQueue;
    private boolean lastRunCommandQueue;
    private boolean runCommandQueue;

    private EAcquisitionState mAcquisitionState = EAcquisitionState.STOWED;
    private EAcquisitionState mRequestedAcquistionState = EAcquisitionState.STOWED;
    private EScoreState mScoringState = EScoreState.NONE;
    private EScoreState mRequestedScoringState = EScoreState.NONE;

    private boolean mHatchGrabberExtendRequested = false;
    private boolean mHatchGrabberGrabRequested = false;

    private Elevator mElevator;
    private Intake mIntake;
    private HatchFlower mHatchFlower;
    private CargoSpit mCargoSpit;

    public Superstructure(Elevator pElevator, Intake pIntake, HatchFlower pHatchFlower, CargoSpit pCargoSpit) {
        mElevator = pElevator;
        mIntake = pIntake;
        mHatchFlower = pHatchFlower;
        mCargoSpit = pCargoSpit;
        mDesiredCommandQueue = new CommandQueue();
    }

    public enum EAcquisitionState {
        LOADING_STATION_HATCH,
        LOADING_STATION_CARGO,
        GROUND_HATCH,
        GROUND_CARGO,
        HANDOFF,
        STOWED
    }

    public enum EScoringState {
        HATCH,
        CARGO,
        CLIMB,
        NONE
    }

    @Override
    public void modeInit(double pNow) {
        runCommandQueue = lastRunCommandQueue = false;
    }

    @Override
    public void periodicInput(double pNow) {

    }

    @Override
    public void update(double pNow) {
        updateCommands(pNow);
        updateRobotState();
    }

    private void updateCommands(double pNow) {

        // Don't initialize and update on same cycle
        if (shouldInitializeCommandQueue()) {
            mLog.warn("Initializing command queue");
            mDesiredCommandQueue.init(pNow);
        } else if(isRunningCommands()) {
            mDesiredCommandQueue.update(pNow);
        }

        // Only check if we're done with queue if we're actually running...otherwise we're just spamming stopRunningCommands()
        if(isRunningCommands() && mDesiredCommandQueue.isDone()) {
            mLog.warn("Command queue has completed execution");
            stopRunningCommands();
        }

        lastRunCommandQueue = runCommandQueue;
    }

    private void updateRobotState() {

        handleRequestedRobotState();
        handleCurrentRobotState();

        // If we are handing off we don't want the grabber to collide with the hatch
        if(mHatchGrabberGrabRequested && mAcquisitionState != EAcquisitionState.HANDOFF) {
            mHatchFlower.captureHatch();
        }


        // No collisions here
        if(mHatchGrabberExtendRequested) {
            mHatchFlower.setFlowerExtended(true);
        }

    }

    /**
     * Handles transitions between states by running when the requested state is
     * changed and sets up mechnanisms to acquire game pieces.
     */
    private void handleRequestedRobotState() {

        if(mRequestedAcquistionState != mAcquisitionState) {
            switch(mRequestedAcquistionState) {
                case LOADING_STATION_HATCH:

                    // Set elevator to bottom, extend hatch grabber
                    mElevator.setStatePosition(EElevatorPosition.BOTTOM);
                    mHatchFlower.setFlowerExtended(true);

                    break;
                case LOADING_STATION_CARGO:
                    // TODO Is this even possible?
                    break;
                case GROUND_HATCH:

                    // Set elevator to bottom, extend hatch grabber, start intaking
                    mElevator.setStatePosition(EElevatorPosition.BOTTOM);
                    mHatchFlower.setFlowerExtended(true);
                    mIntake.setIntakingHatch();

                    break;
                case GROUND_CARGO:

                    // Set elevator to bottom, retract hatch grabber, start intaking with both ground intake and cargo spit
                    mElevator.setStatePosition(EElevatorPosition.BOTTOM);
                    mHatchFlower.setFlowerExtended(false); // TODO Check
                    mIntake.setIntakingCargo();
                    mCargoSpit.setIntaking();


                    break;
                case HANDOFF:

                    // Set elevator to bottom
                    mElevator.setStatePosition(EElevatorPosition.BOTTOM);

                    // Tell intake to handoff depending on what we picked up
                    if(mAcquisitionState == EAcquisitionState.GROUND_HATCH) {
                        mHatchFlower.setFlowerExtended(true);
                        mIntake.setHandoffHatch();
                    } else if(mAcquisitionState == EAcquisitionState.GROUND_CARGO) {
                        mHatchFlower.setFlowerExtended(false); // TODO Check
                        mIntake.setHandoffCargo();
                    }

                    break;
                case STOWED:

                    // TODO Is there a collision here? mHatchFlower.setFlowerExtended(false);
                    mIntake.stow();

                    break;
            }

            // Update state to requested
            mAcquisitionState = mRequestedAcquistionState;
        }

        if(mRequestedScoringState != mScoringState) {
            switch(mRequestedScoringState) {
                case SCORE_CARGO:
                    break;
                case SCORE_HATCH:
                    break;
                case NONE:
                    break;
            }

            mScoringState = mRequestedScoringState;
        }

    }

    /**
     * Checks if the current state is finished and we can move to the next state.
     */
    private void handleCurrentRobotState() {
        switch(mAcquisitionState) {
            case LOADING_STATION_HATCH:
                // Any further automation is handled directly by hatch grabber
                break;
            case LOADING_STATION_CARGO:
                // TODO
                break;
            case GROUND_HATCH:

                if(mElevator.isAtPosition(EElevatorPosition.BOTTOM) && mHatchFlower.isExtended() && mIntake.hasHatch()) {
                    mRequestedAcquistionState = EAcquisitionState.HANDOFF;
                }

                break;
            case GROUND_CARGO:

                if(mCargoSpit.hasCargo()) {
                    mRequestedAcquistionState = EAcquisitionState.HANDOFF;
                }

                break;
            case HANDOFF:

                // TODO Solve any interference?
                if(mHatchFlower.hasHatch() || mCargoSpit.hasCargo()) {
                    mRequestedAcquistionState = EAcquisitionState.STOWED;
                }

                break;
            case STOWED:

                // Do nothing

                break;
        }

        /*
        These are set every cycle as opposed to once when the state is requested
        so that we can stop the sequence if we want to acquire a game piece.
         */
        switch(mScoringState) {
            case SCORE_CARGO:

                mCargoSpit.setOuttaking();

                break;
            case SCORE_HATCH:

                // Don't interrupt handoff
                if(mAcquisitionState != EAcquisitionState.HANDOFF) {
                    mHatchFlower.pushHatch();
                }

                break;
            case NONE:

                /*
                If we aren't requesting to score, make sure all motors are stopped.
                This should be handled automatically by each module, but provides a
                safety in case sensors are broken.
                 */
                if(mAcquisitionState == EAcquisitionState.STOWED) {
                    mIntake.stop();
                    mCargoSpit.stop();
                }

                break;
        }
    }


    @Override
    public void shutdown(double pNow) {

    }

    public boolean isRunningCommands() {
        return runCommandQueue;
    }

    /**
     * If we weren't running commands last cycle, initialize.
     */
    public boolean shouldInitializeCommandQueue() {
        return lastRunCommandQueue == false && runCommandQueue == true;
    }

    public CommandQueue getDesiredCommandQueue() {
        return mDesiredCommandQueue;
    }

    public void startCommands(ICommand ... pCommands) {
        // Only update the command queue if commands aren't already running
        if(!isRunningCommands()) {
            mLog.warn("Starting superstructure command queue with a size of ", pCommands.length);
            runCommandQueue = true;
            mDesiredCommandQueue.setCommands(pCommands);
        } else {
            mLog.warn("Set commands was called, but superstructure is already running commands");
        }
    }

    public void stopRunningCommands() {
        mLog.warn("Stopping command queue");
        runCommandQueue = false;
        mDesiredCommandQueue.clear();
    }

    public void setIntaking(EAcquisitionState pAcquisitionState) {
        mAcquisitionState = pAcquisitionState;
    }

    public void setScoring(EScoreState pScoringState) {
        mScoringState = pScoringState;
    }

    public void extendHatchGrabber(boolean pHatchGrabberExtendRequested) {
        mHatchGrabberExtendRequested = pHatchGrabberExtendRequested;
    }

    public void grabHatch(boolean pHatchGrabberGrabRequested) {
        mHatchGrabberGrabRequested = pHatchGrabberGrabRequested;
    }



}
