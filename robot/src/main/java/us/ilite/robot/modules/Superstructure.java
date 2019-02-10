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

    EAcquisitionState mAcquisitionState = EAcquisitionState.STOWED;
    EAcquisitionState mRequestedAcquisitionState = EAcquisitionState.STOWED;
    private EScoringState mScoringState = EScoringState.NONE;
    private EScoringState mRequestedScoringState = EScoringState.NONE;

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

        mLog.info("Starting Current State: ", mAcquisitionState);

        handleRequestedRobotState();
        handleCurrentRobotState();

        boolean handlingCargo = mAcquisitionState == EAcquisitionState.GROUND_CARGO || mAcquisitionState == EAcquisitionState.LOADING_STATION_CARGO;

        if(mAcquisitionState != EAcquisitionState.HANDOFF && !handlingCargo) {
            // If we are handing off we don't want the grabber to collide with the hatch
            if(mHatchGrabberGrabRequested) {
                mHatchFlower.captureHatch();
            }

            // No collisions here
            if(mHatchGrabberExtendRequested) {
                mHatchFlower.setFlowerExtended(true);
            }
        }

        mLog.info("Ending Current State: ", mAcquisitionState);
    }

    /**
     * Handles transitions between states by running when the requested state is
     * changed and sets up mechnanisms to acquire game pieces.
     */
    private void handleRequestedRobotState() {

        // Driver can't command a new state while we are handing off
        if(mRequestedAcquisitionState != mAcquisitionState && mAcquisitionState != EAcquisitionState.HANDOFF) {

            mLog.info("Switching to new acquisition state: ", mRequestedAcquisitionState, " from state: ", mAcquisitionState);
            mAcquisitionState = mRequestedAcquisitionState;

        } else {

            mLog.info("Not switching to acquisition state ", mRequestedAcquisitionState, " from state: ", mAcquisitionState);

        }

        if(mRequestedScoringState != mScoringState) {
            mScoringState = mRequestedScoringState;
        }

    }

    /**
     * Checks if the current state is finished and we can move to the next state.
     */
    private void handleCurrentRobotState() {

        mLog.info("Updating state: ", mAcquisitionState);
        switch(mAcquisitionState) {
            case LOADING_STATION_HATCH:

                // Set elevator to bottom, extend hatch grabber
                mElevator.setStatePosition(EElevatorPosition.BOTTOM);
                mHatchFlower.setFlowerExtended(true);
                // Any further automation is handled directly by hatch grabber

                break;
            case LOADING_STATION_CARGO:
                // TODO Is this even possible?
                break;
            case GROUND_HATCH:

                // Set elevator to bottom, extend hatch grabber, start intaking
                mElevator.setStatePosition(EElevatorPosition.BOTTOM);
                mHatchFlower.setFlowerExtended(true);

                if(mHatchFlower.isExtended() && mIntake.hasHatch() && mElevator.isAtPosition(EElevatorPosition.BOTTOM)) {
                    mAcquisitionState = EAcquisitionState.HANDOFF;
                }

                break;
            case GROUND_CARGO:

                // Set elevator to bottom, retract hatch grabber, start intaking with both ground intake and cargo spit
                mElevator.setStatePosition(EElevatorPosition.BOTTOM);
                mHatchFlower.setFlowerExtended(false); // TODO Check

                if(!mHatchFlower.isExtended() && mElevator.isAtPosition(EElevatorPosition.BOTTOM)) {
                    mIntake.setIntakingCargo();
                    mCargoSpit.setIntaking();
                }

                if(mCargoSpit.hasCargo()) {
                    mAcquisitionState = EAcquisitionState.HANDOFF;
                }

                break;
            case HANDOFF:

                // Set elevator to bottom
                mElevator.setStatePosition(EElevatorPosition.BOTTOM);

                // Tell intake to handoff depending on what we picked up
                if (mAcquisitionState == EAcquisitionState.GROUND_HATCH) {
                    mHatchFlower.setFlowerExtended(true);

                    if(mHatchFlower.isExtended()) {
                        mIntake.setHandoffHatch();
                    }

                } else if (mAcquisitionState == EAcquisitionState.GROUND_CARGO) {
                    mHatchFlower.setFlowerExtended(false); // TODO Check

                    if(!mHatchFlower.isExtended()) {
                        mIntake.setHandoffCargo();
                    }

                } else {
                    mLog.error("Invalid handoff state");
                }

                // TODO Solve any interference?
                if(mHatchFlower.hasHatch() || mCargoSpit.hasCargo()) {
                    mAcquisitionState = EAcquisitionState.STOWED;
                }

                break;
            case STOWED:

                // TODO Is there a collision here? mHatchFlower.setFlowerExtended(false);
                mIntake.stow();
                // Do nothing

                break;
        }

        /*
        These are set every cycle as opposed to once when the state is requested
        so that we can stop the sequence if we want to acquire a game piece.
         */
        switch(mScoringState) {
            case CARGO:

                mHatchFlower.setFlowerExtended(false);

                if(!mHatchFlower.isExtended()) {
                    mCargoSpit.setOuttaking();
                }

                break;
            case HATCH:

                // Don't interrupt handoff
                if(mAcquisitionState != EAcquisitionState.HANDOFF) {
                    mHatchFlower.pushHatch();
                }

                break;
            case CLIMB:
                break;
            case NONE:

                /*
                If we aren't requesting to score, make sure all motors are stopped.
                This should be handled automatically by each module, but provides a
                safety in case sensors are broken or not used.
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

    public void requestIntaking(EAcquisitionState pAcquisitionState) {
        mRequestedAcquisitionState = pAcquisitionState;
    }

    public void requestIntaking(EScoringState pScoringState) {
        mRequestedScoringState = pScoringState;
    }

    public void extendHatchGrabber(boolean pHatchGrabberExtendRequested) {
        mHatchGrabberExtendRequested = pHatchGrabberExtendRequested;
    }

    public void grabHatch(boolean pHatchGrabberGrabRequested) {
        mHatchGrabberGrabRequested = pHatchGrabberGrabRequested;
    }

    private void setAcquisitionState(EAcquisitionState pAcquisitionState) {
        // Update state to requested
        mAcquisitionState = mRequestedAcquisitionState;
        mLog.info("Changed superstructure for current state ", mAcquisitionState, " to requested state ", mRequestedAcquisitionState);
    }

    public EAcquisitionState getAcquisitionState() {
        return mAcquisitionState;
    }

    public EAcquisitionState getRequestedAcquisitionState() {
        return mRequestedAcquisitionState;
    }

    public EScoringState getScoringState() {
        return mScoringState;
    }

    public EScoringState getRequestedScoringState() {
        return mRequestedScoringState;
    }

}
