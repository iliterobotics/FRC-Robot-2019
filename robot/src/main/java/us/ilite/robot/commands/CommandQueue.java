package us.ilite.robot.commands;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Handles the initialization, updating, and shutdown of commands. Since CommandQueue is a
 * command itself, it's guaranteed to have the same methods that a normal command does and
 * can be used as a command itself (useful for defining a commonly used sequence of actions,
 * like "put the elevator up and score a cube" or "lower the intake, raise the shooter and score a ball").
 */
public class CommandQueue implements ICommand {

    private ILog mLogger = Logger.createLog(CommandQueue.class);
    private Queue<ICommand> mCommandQueue = new LinkedList<>();


    @Override
    public void init(double pNow) {
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
                // Shutdown the current command, grab the next one and initialize it.
                mCurrentCommand.shutdown(pNow);
                mCommandQueue.poll();
                initCurrentCommand(pNow);
            }

        } else if(mCommandQueue.isEmpty()) {
            mLogger.warn("Command queue is empty.");
        } else {
            mLogger.error("Ran into null command.");
        }

        if(isFinished()) {
            return true;
        }

        return false;
    }

    private void initCurrentCommand(double pNow) {
        if(mCommandQueue.peek() != null) mCommandQueue.peek().init(pNow);
    }

    @Override
    public void shutdown(double pNow) {

    }

    public boolean isFinished() {
        return mCommandQueue.isEmpty();
    }

    public void setCommands(ICommand ... pCommands) {
        mCommandQueue.clear();
        mCommandQueue.addAll(Arrays.asList(pCommands));
    }

}
