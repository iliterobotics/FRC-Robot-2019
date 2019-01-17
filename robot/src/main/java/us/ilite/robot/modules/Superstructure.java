package us.ilite.robot.modules;

import us.ilite.robot.commands.CommandQueue;
import us.ilite.robot.commands.ICommand;

public class Superstructure extends Module {

    private CommandQueue desiredCommandQueue;
    private boolean lastRunCommandQueue;
    private boolean runCommandQueue;

    public Superstructure() {
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
        if(desiredCommandQueue.isDone()) {
            stopRunningCommands();
        }
        if (shouldInitializeCommandQueue()) {
            desiredCommandQueue.init(pNow);
        }
        if(isRunningCommands()) {
            desiredCommandQueue.update(pNow);
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

    public void setCommands(ICommand ... pCommands) {
        // Only update the command queue if commands aren't already running
        if(!isRunningCommands()) {
            runCommandQueue = true;
            desiredCommandQueue.setCommands(pCommands);
        }
    }

    public void stopRunningCommands() {
        runCommandQueue = false;
        desiredCommandQueue.clear();
    }

}
