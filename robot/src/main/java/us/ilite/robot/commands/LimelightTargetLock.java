package us.ilite.robot.commands;

import us.ilite.common.types.ETrackingType;
import us.ilite.robot.modules.Drive;
import us.ilite.robot.modules.IThrottleProvider;
import us.ilite.robot.modules.Limelight;
import us.ilite.robot.modules.targetData.ITargetDataProvider;

public class LimelightTargetLock extends TargetLock {

    private Limelight mLimelight;

    public LimelightTargetLock(Drive pDrive, Limelight pLimelight, double pAllowableError, ETrackingType pTrackingType, ITargetDataProvider pCamera, IThrottleProvider pThrottleProvider) {
        super(pDrive, pAllowableError, pTrackingType, pCamera, pThrottleProvider);
        this.mLimelight = pLimelight;
        mLimelight.setPipeline(pTrackingType.getPipeline());
    }

    public LimelightTargetLock(Drive pDrive, Limelight pLimelight, double pAllowableError, ETrackingType pTrackingType, ITargetDataProvider pCamera, IThrottleProvider pThrottleProvider, boolean pEndOnAlignment) {
        super(pDrive, pAllowableError, pTrackingType, pCamera, pThrottleProvider, pEndOnAlignment);

        this.mLimelight = pLimelight;
        mLimelight.setPipeline(pTrackingType.getPipeline());
    }

}
