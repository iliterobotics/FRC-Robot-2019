package us.ilite.robot.commands;

public class InstantCommand extends ACommand {

    private final Runnable mFunctionToInvoke;

    public InstantCommand(Runnable pFunctionToInvoke) {
        mFunctionToInvoke = pFunctionToInvoke;
    }

    @Override
    public void init(double pNow) {
        mFunctionToInvoke.run();
    }

    @Override
    public boolean update(double pNow) {
        return true;
    }

    @Override
    public void shutdown(double pNow) {

    }

    public void test() {}


}
