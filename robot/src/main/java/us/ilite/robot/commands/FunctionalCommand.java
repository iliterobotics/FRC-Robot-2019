package us.ilite.robot.commands;

import java.util.function.BooleanSupplier;

/**
 * Runs
 */
public class FunctionalCommand implements ICommand {

    private final Runnable mFunctionToInvoke;
    private final BooleanSupplier mEndCondition;

    public FunctionalCommand(Runnable pFunctionToInvoke, BooleanSupplier pEndCondition) {
        mFunctionToInvoke = pFunctionToInvoke;
        mEndCondition = pEndCondition;
    }

    public FunctionalCommand(Runnable pFunctionToInvoke) {
        this(pFunctionToInvoke, () -> true);
    }

    @Override
    public void init(double pNow) {
        mFunctionToInvoke.run();
    }

    @Override
    public boolean update(double pNow) {
        return mEndCondition.getAsBoolean();
    }

    @Override
    public void shutdown(double pNow) {
    }

}
