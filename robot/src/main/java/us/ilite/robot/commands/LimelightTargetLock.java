package us.ilite.robot.commands;

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
        mLimelight.setPipeline(pTrackingType.getPipeline());
        mLimelight.setLedMode(pTrackingType.getLedOn() ? Limelight.LedMode.LED_ON : Limelight.LedMode.LED_OFF);
    }

    public LimelightTargetLock(Drive pDrive, Limelight pLimelight, double pAllowableError, ETrackingType pTrackingType, IThrottleProvider pThrottleProvider, boolean pEndOnAlignment) {
        super(pDrive, pAllowableError, pTrackingType, pLimelight, pThrottleProvider, pEndOnAlignment);

        this.mLimelight = pLimelight;
        mLimelight.setPipeline(pTrackingType.getPipeline());
        mLimelight.setLedMode(pTrackingType.getLedOn() ? Limelight.LedMode.LED_ON : Limelight.LedMode.LED_OFF);
    }

    public void shutdown(double pNow) {
        super.shutdown(pNow);
        mLimelight.setLedMode(Limelight.LedMode.LED_OFF);
        mLimelight.setPipeline(ETrackingType.NONE.getPipeline());
    }

}
