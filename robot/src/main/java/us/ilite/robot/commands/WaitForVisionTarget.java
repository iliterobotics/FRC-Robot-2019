package us.ilite.robot.commands;

import us.ilite.common.Data;
import us.ilite.common.types.ETargetingData;
import us.ilite.common.types.ETrackingType;
import us.ilite.robot.modules.Limelight;

public class WaitForVisionTarget implements ICommand {

    private Data mData;
    private Limelight mLimelight;
    private double mTargetAreaThreshold;

    public WaitForVisionTarget(Data pData, Limelight pLimelight, double pTargetAreaThreshold) {
        mData = pData;
        mLimelight = pLimelight;
        mTargetAreaThreshold = pTargetAreaThreshold;
    }

    @Override
    public void init(double pNow) {
        mLimelight.setTracking(ETrackingType.TARGET);
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
        mLimelight.setTracking(ETrackingType.NONE);
    }
}
