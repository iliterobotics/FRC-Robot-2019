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
    private EElevatorPosition mRequestedElevatorPosition = EElevatorPosition.BOTTOM;

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

        /*
        We run code for switching to a new state based on driver input only once
        This ensures that if we are intaking and transition to handoff the driver controls don't override the handoff state and force us back to a intaking state
        We can't switch states under any circumstance if we are handing off
         */
        if(mRequestedAcquisitionState != mAcquisitionState && mAcquisitionState != EAcquisitionState.HANDOFF) {

            mLog.info("Switching to new acquisition state: ", mRequestedAcquisitionState, " from state: ", mAcquisitionState);
            mAcquisitionState = mRequestedAcquisitionState;

        } else {

            mLog.info("Not switching to acquisition state ", mRequestedAcquisitionState, " from state: ", mAcquisitionState);

        }

        /*
        Only switch scoring states once
        Only go to a scoring state if we aren't handing off or intaking
        */
        if(mRequestedScoringState != mScoringState &&
                mAcquisitionState != EAcquisitionState.HANDOFF &&
                mAcquisitionState != EAcquisitionState.GROUND_CARGO &&
                mAcquisitionState != EAcquisitionState.GROUND_HATCH) {
            mElevator.setDesirecPosition(mRequestedElevatorPosition);
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

                // Set elevator to bottom so we can grab hatch
                mElevator.setDesirecPosition(EElevatorPosition.BOTTOM);
                // Extend grabber so we can grab hatch
                mHatchFlower.setFlowerExtended(true);
                // Any further automation is handled directly by hatch grabber

                break;
            case LOADING_STATION_CARGO:
                // TODO Is this even possible?
                break;
            case GROUND_HATCH:

                // Set elevator to bottom so we can grab hatch with hatch grabber
                mElevator.setDesirecPosition(EElevatorPosition.BOTTOM);
                // Extend grabber so it can grab hatch when we handoff
                mHatchFlower.setFlowerExtended(true);
                // Tell the intake to intake a hatch
                mIntake.setIntakingHatch();

                /**
                 * Handoff when:
                 * Hatch grabber is fully extended
                 * We have the hatch in the intake
                 * The elevator is down
                 */
                if(mHatchFlower.isExtended() && mIntake.hasHatch() && mElevator.isAtPosition(EElevatorPosition.BOTTOM)) {
                    mAcquisitionState = EAcquisitionState.HANDOFF;
                }

                break;
            case GROUND_CARGO:

                // Set elevator to bottom so the cargo spit can
                mElevator.setDesirecPosition(EElevatorPosition.BOTTOM);
                // Retract hatch grabber so it doesn't hit the ball
                mHatchFlower.setFlowerExtended(false); // TODO Check

                /**
                 * Start intaking cargo when:
                 * Hatch grabber is fully retracted
                 * Elevator at bottom
                 */
                if(!mHatchFlower.isExtended() && mElevator.isAtPosition(EElevatorPosition.BOTTOM)) {
                    mIntake.setIntakingCargo();
                    mCargoSpit.setIntaking();
                }

                /**
                 * If we have the cargo, bring the intake into a handoff state so we can check for collisions
                 * before stowing the intake
                 */
                if(mCargoSpit.hasCargo()) {
                    mAcquisitionState = EAcquisitionState.HANDOFF;
                }

                break;
            case HANDOFF:

                // Set elevator to bottom so we can hand off from intake to (insert mechanism here)
                mElevator.setDesirecPosition(EElevatorPosition.BOTTOM);

                // Tell intake to handoff depending on what we asked to pick up
                if (mRequestedAcquisitionState == EAcquisitionState.GROUND_HATCH) {

                    // Make sure that grabber is extended
                    mHatchFlower.setFlowerExtended(true);

                    // Wait until hatch grabber is extended to receive hatch
                    if(mHatchFlower.isExtended()) {
                        // Bring intake to handoff position when hatch grabber is ready
                        mIntake.setHandoffHatch();

                        // If the intake is ready to hand off and elevator is still down, grab the hatch
                        // TODO If we have a sensor here this can be offloaded to the hatch grabber class
                        if(mIntake.isAtPosition(Intake.EWristPosition.HANDOFF) && mElevator.isAtPosition(EElevatorPosition.BOTTOM)) {
                            mHatchFlower.captureHatch();
                        }
                    }

                } else if (mRequestedAcquisitionState == EAcquisitionState.GROUND_CARGO) {
                    // Retract the hatch grabber so we don't hit the ball
                    mHatchFlower.setFlowerExtended(false);

                    if(!mHatchFlower.isExtended()) {
                        // Bring to intake to handoff position
                        mIntake.setHandoffCargo();
                    }

                } else {
                    mLog.error("Invalid handoff state");
                }

                // TODO Solve any interference?
                // If we have the gamepiece then raise the elevator and stow
                if(mHatchFlower.hasHatch() || mCargoSpit.hasCargo()) {
                    mElevator.setDesirecPosition(EElevatorPosition.HANDOFF_HEIGHT);

                    if(mElevator.isAbovePosition(EElevatorPosition.HANDOFF_HEIGHT)) {
                        mAcquisitionState = EAcquisitionState.STOWED;
                    }
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

                // Retract the hatch grabber if not already
                mHatchFlower.setFlowerExtended(false);

                // Shoot cargo when hatch grabber out of the way
                if(!mHatchFlower.isExtended()) {
                    mCargoSpit.setOuttaking();
                }

                break;
            case HATCH:

                // Extend hatch grabber if not extended
                mHatchFlower.setFlowerExtended(true);

                // Kick hatch if not already
                if(mHatchFlower.isExtended()) {
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

    /*
    Requests
     */
    public void requestIntaking(EAcquisitionState pAcquisitionState) {
        mRequestedAcquisitionState = pAcquisitionState;
    }

    public void requestScoring(EScoringState pScoringState) {
        mRequestedScoringState = pScoringState;
    }

    public void requestElevator(EElevatorPosition pElevatorPosition) {
        mRequestedElevatorPosition = pElevatorPosition;
    }

    public void extendHatchGrabber(boolean pHatchGrabberExtendRequested) {
        mHatchGrabberExtendRequested = pHatchGrabberExtendRequested;
    }

    public void grabHatch(boolean pHatchGrabberGrabRequested) {
        mHatchGrabberGrabRequested = pHatchGrabberGrabRequested;
    }

    /*
    Getters
     */
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
