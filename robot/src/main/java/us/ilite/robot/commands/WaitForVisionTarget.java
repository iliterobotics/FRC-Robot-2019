package us.ilite.robot.commands;

import us.ilite.common.Data;
import us.ilite.common.types.ETargetingData;

public class WaitForVisionTarget implements ICommand {

    private Data mData;
    private double mTargetAreaThreshold;

    public WaitForVisionTarget(Data mData, double mTargetAreaThreshold) {
        this.mData = mData;
        this.mTargetAreaThreshold = mTargetAreaThreshold;
    }

    @Override
    public void init(double pNow) {

    }

    @Override
    public boolean update(double pNow) {

        // If target is valid
        if(mData.limelight.isSet(ETargetingData.tv)) {
            // If area above threshold exit command
            if(mData.limelight.get(ETargetingData.ta) > mTargetAreaThreshold) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void shutdown(double pNow) {

    }
}
