package us.ilite.robot.modules;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import us.ilite.robot.commands.CommandQueue;
import us.ilite.robot.commands.ICommand;

public class Superstructure extends Module {

    private ILog mLog = Logger.createLog(Superstructure.class);

    private CommandQueue mDesiredCommandQueue = new CommandQueue();;
    private boolean lastRunCommandQueue;
    private boolean runCommandQueue;

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

    public void startCommands(ICommand... pCommands) {
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

}
