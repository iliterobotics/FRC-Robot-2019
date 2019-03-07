package us.ilite.robot.commands;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class CommandQueue implements ICommand {

    private ILog mLogger = Logger.createLog(CommandQueue.class);
    private Queue<ICommand> mCommandQueue = new LinkedList<>();


    @Override
    public void init(double pNow) {
        mLogger.info("Initializing command queue");
        // Initialize the first command
        initCurrentCommand(pNow);
    }

    @Override
    public boolean update(double pNow) {
        // Grab the current command
        ICommand mCurrentCommand = mCommandQueue.peek();

        // Check that we aren't at the end of the queue or that a null pointer won't occur
        if(mCurrentCommand != null) {
            // If command finished
            if(mCurrentCommand.update(pNow)) {
                mLogger.info("Command finished.");
                mCurrentCommand.shutdown(pNow);
                mCommandQueue.poll();
                initCurrentCommand(pNow);
            }

        } else if(isDone()) {
            return true;
        } else {
            mLogger.error("Ran into null command.");
        }

        return false;
    }

    private void initCurrentCommand(double pNow) {
        if(mCommandQueue.peek() != null) {
            mLogger.info("Initializing next command");
            mCommandQueue.peek().init(pNow);
        } else {
            mLogger.info("Can't initialize next command: queue is empty");
        }
    }

    @Override
    public void shutdown(double pNow) {
        if(mCommandQueue.peek() != null) mCommandQueue.peek().shutdown(pNow);
    }

    public void setCommands(ICommand ... pCommands) {
        mCommandQueue.clear();
        mCommandQueue.addAll(Arrays.asList(pCommands));
    }

    public void clear() {
        mCommandQueue.clear();
    }

    public boolean isDone() {
        return mCommandQueue.isEmpty();
    }

}
