package us.ilite.robot.modules;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import edu.wpi.first.wpilibj.Timer;
import us.ilite.common.config.SystemSettings;
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

        switch(mRequestedAcquistionState) {
            case LOADING_STATION:
                break;
            case GROUND_HATCH:
                break;
            case GROUND_CARGO:
                break;
            case HANDING_OFF:
                break;
            case STOWED:
                break;
        }

//        switch(mRequestedAcquistionState) {
//            case LOADING_STATION:
//
//                mIntake.stow();
//                mElevator.setStatePosition(EElevatorPosition.BOTTOM);
//                mHatchFlower.setFlowerExtended(true);
//
//                // Update state
//                mAcquisitionState = EAcquisitionState.LOADING_STATION;
//                break;
//            case GROUND_HATCH:
//
//                mElevator.setStatePosition(EElevatorPosition.BOTTOM);
//                mHatchFlower.setFlowerExtended(true);
//                mIntake.setIntakingHatch();
//
//                if(mElevator.isAtPosition(EElevatorPosition.BOTTOM) && mIntake.hasHatch()) {
//                    mRequestedAcquistionState = EAcquisitionState.HANDING_OFF;
//                }
//
//                mAcquisitionState = EAcquisitionState.GROUND_HATCH;
//                break;
//            case GROUND_CARGO:
//
//                mElevator.setStatePosition(EElevatorPosition.BOTTOM);
//                mHatchFlower.setFlowerExtended(false);
//
//                if(mElevator.isAtPosition(EElevatorPosition.BOTTOM)) {
//                    mIntake.setIntakingCargo();
//                    mCargoSpit.setIntaking();
//                }
//
//                if(mCargoSpit.hasCargo()) {
//                    mRequestedAcquistionState = EAcquisitionState.HANDING_OFF;
//                }
//
//                mAcquisitionState = EAcquisitionState.GROUND_CARGO;
//                break;
//            case HANDING_OFF:
//
//                mElevator.setStatePosition(EElevatorPosition.BOTTOM);
//
//                if(mAcquisitionState.equals(EAcquisitionState.GROUND_HATCH)) {
//                    mHatchFlower.setFlowerExtended(true);
//                    mIntake.setHandoffHatch();
//                    if(mIntake.isAtPosition(Intake.EWristPosition.HANDOFF)) {
//                        if(mHatchFlower.isExtended()) {
//                            mHatchFlower.captureHatch();
//                        }
//                    }
//                }
//
//                if(mAcquisitionState.equals(EAcquisitionState.GROUND_CARGO)) {
//                    mIntake.setHandoffCargo();
//                    mCargoSpit.setIntaking();
//
//                    if(mCargoSpit.hasCargo()) {
//                        mRequestedAcquistionState =
//                    }
//                }
//
//                mAcquisitionState = EAcquisitionState.HANDING_OFF;
//                break;
//            case STOWED:
//
//                mAcquisitionState = EAcquisitionState.STOWED;
//                break;
//            default:
//                break;
//        }

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

    public void setIntaking(EAcquisitionState pIntakeState) {

    }

    public void setScoring(EScoreState pScoringState) {

    }

}
