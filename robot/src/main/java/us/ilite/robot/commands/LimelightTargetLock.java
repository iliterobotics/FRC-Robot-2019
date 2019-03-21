package us.ilite.robot.commands;

import us.ilite.common.config.SystemSettings;
import us.ilite.common.types.ETrackingType;
import us.ilite.robot.modules.Drive;
import us.ilite.robot.modules.IThrottleProvider;
import us.ilite.robot.modules.Limelight;
import us.ilite.robot.modules.targetData.ITargetDataProvider;

public class LimelightTargetLock extends TargetLock {

    private Limelight mLimelight;

    public LimelightTargetLock(Drive pDrive, Limelight pLimelight, double pAllowableError, ETrackingType pTrackingType, IThrottleProvider pThrottleProvider) {
        super(pDrive, pAllowableError, pTrackingType, pLimelight, pThrottleProvider);
        this.mLimelight = pLimelight;
        mLimelight.setTracking(pTrackingType);
    }

    public LimelightTargetLock(Drive pDrive, Limelight pLimelight, double pAllowableError, ETrackingType pTrackingType, IThrottleProvider pThrottleProvider, boolean pEndOnAlignment) {
        super(pDrive, pAllowableError, pTrackingType, pLimelight, pThrottleProvider, pEndOnAlignment);

        this.mLimelight = pLimelight;
        mLimelight.setTracking(pTrackingType);
    }

    public LimelightTargetLock setVisionTarget(SystemSettings.VisionTarget pVisionTarget) {
        mLimelight.setVisionTarget(pVisionTarget);

        return this;
    }

    public void shutdown(double pNow) {
        super.shutdown(pNow);
        mLimelight.setTracking(ETrackingType.NONE);
    }

}
