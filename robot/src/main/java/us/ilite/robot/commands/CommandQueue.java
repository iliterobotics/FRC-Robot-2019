package us.ilite.robot.commands;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class CommandQueue implements ICommand {

    private ILog mLogger = Logger.createLog(CommandQueue.class);

    private boolean firstRun = false;
    private Queue<ICommand> mCommandQueue = new LinkedList<>();


    @Override
    public void init(double pNow) {
    }

    @Override
    public boolean update(double pNow) {
        // Grab the next command
        ICommand mCurrentCommand = mCommandQueue.peek();

        // Check that we aren't at the end of the queue or that a null pointer won't occur
        if(mCurrentCommand != null) {

            // Initialize the command
            mCurrentCommand.init(pNow);

            if(mCurrentCommand.update(pNow)) {
                mCommandQueue.poll();
            }

        } else if(mCommandQueue.isEmpty()) {
            mLogger.warn("Command queue is empty.");
        } else {
            mLogger.error("Ran into null command.");
        }


        return false;
    }

    @Override
    public void shutdown(double pNow) {

    }

    public void setCommands(ICommand ... pCommands) {
        mCommandQueue.clear();
        mCommandQueue.addAll(Arrays.asList(pCommands));
        firstRun = false;
    }

}
