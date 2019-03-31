package us.ilite.robot.modules;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import us.ilite.robot.commands.CommandQueue;
import us.ilite.robot.commands.ICommand;

/**
 * Provides a wrapper for CommandQueue that allows commands to be stopped and started at will.
 */
public class CommandManager extends Module {

    private ILog mLog = Logger.createLog(CommandManager.class);

    private String mManagerTag = "";

    private CommandQueue desiredCommandQueue;
    private boolean lastRunCommandQueue;
    private boolean runCommandQueue;

    public CommandManager() {
        this.desiredCommandQueue = new CommandQueue();
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
            mLog.warn(mManagerTag, ": Initializing command queue");
            desiredCommandQueue.init(pNow);
        } else if(isRunningCommands()) {
            desiredCommandQueue.update(pNow);
        }

        // Only check if we're done with queue if we're actually running...otherwise we're just spamming stopRunningCommands()
        if(isRunningCommands() && desiredCommandQueue.isDone()) {
            mLog.warn(mManagerTag, ": Command queue has completed execution");
            stopRunningCommands(pNow);
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
        return desiredCommandQueue;
    }

    public void startCommands(ICommand ... pCommands) {
        // Only update the command queue if commands aren't already running
        if(!isRunningCommands()) {
            mLog.warn(mManagerTag, ": Starting command queue with a size of ", pCommands.length);
            runCommandQueue = true;
            desiredCommandQueue.setCommands(pCommands);
        } else {
            mLog.warn(mManagerTag, ": Set commands was called, but superstructure is already running commands");
        }
    }

    public void stopRunningCommands(double pNow) {
        mLog.warn(mManagerTag, ": Stopping command queue");
        runCommandQueue = false;
        desiredCommandQueue.shutdown(pNow);
        desiredCommandQueue.clear();
    }

    public CommandManager setManagerTag(String pManagerTag) {
        mManagerTag = pManagerTag;
        return this;
    }

}
