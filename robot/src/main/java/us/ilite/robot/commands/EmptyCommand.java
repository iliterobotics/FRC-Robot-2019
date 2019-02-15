package us.ilite.robot.commands;

public class EmptyCommand extends ACommand {

    @Override
    public void init(double pNow) {

    }

    @Override
    public boolean update(double pNow) {
        return true;
    }

    @Override
    public void shutdown(double pNow) {

    }

}
