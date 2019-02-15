package us.ilite.robot.commands;

import us.ilite.robot.modules.HatchFlower;

public class SetHatchGrabberPosition extends ACommand {

    private final HatchFlower mHatchFlower;

    private final boolean mDesiredExtended;

    public SetHatchGrabberPosition(HatchFlower pHatchFlower, boolean pDesiredExtended) {
        mHatchFlower = pHatchFlower;
        mDesiredExtended = pDesiredExtended;
    }

    @Override
    public void init(double pNow) {
        mHatchFlower.setFlowerExtended(true);
    }

    @Override
    public boolean update(double pNow) {
        if(mDesiredExtended) {
            return mHatchFlower.isExtended();
        } else {
            return !mHatchFlower.isExtended();
        }
    }

    @Override
    public void shutdown(double pNow) {

    }

}
