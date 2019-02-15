package us.ilite.robot.commands;

import us.ilite.robot.modules.HatchFlower;

public class GrabHatch extends ACommand {

    private final HatchFlower mHatchFlower;

    public GrabHatch(HatchFlower pHatchFlower) {
        mHatchFlower = pHatchFlower;
    }

    @Override
    public void init(double pNow) {
        mHatchFlower.captureHatch();
    }

    @Override
    public boolean update(double pNow) {
        return true;
    }

    @Override
    public void shutdown(double pNow) {

    }

}
