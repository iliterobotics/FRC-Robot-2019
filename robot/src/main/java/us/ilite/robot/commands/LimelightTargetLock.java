package us.ilite.robot.commands;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import us.ilite.common.types.ETrackingType;
import us.ilite.robot.modules.Drive;
import us.ilite.robot.modules.IThrottleProvider;
import us.ilite.robot.modules.Limelight;
import us.ilite.robot.modules.targetData.ITargetDataProvider;

public class LimelightTargetLock extends TargetLock {

    private ILog mLog = Logger.createLog(LimelightTargetLock.class);

    private Limelight mLimelight;

    public LimelightTargetLock(Drive pDrive, Limelight pLimelight, double pAllowableError, ETrackingType pTrackingType, IThrottleProvider pThrottleProvider) {
        super(pDrive, pAllowableError, pTrackingType, pLimelight, pThrottleProvider);
        this.mLimelight = pLimelight;
        mLimelight.setTracking(pTrackingType);
        mLog.error("STARTED LIMELIGHT TARGET LOCK");
    }

    public LimelightTargetLock(Drive pDrive, Limelight pLimelight, double pAllowableError, ETrackingType pTrackingType, IThrottleProvider pThrottleProvider, boolean pEndOnAlignment) {
        super(pDrive, pAllowableError, pTrackingType, pLimelight, pThrottleProvider, pEndOnAlignment);

        this.mLimelight = pLimelight;
        mLimelight.setTracking(pTrackingType);
        mLog.error("STARTED LIMELIGHT TARGET LOCK");
    }

    public void shutdown(double pNow) {
        super.shutdown(pNow);
        mLog.warn("SHUT DOWN LIMELIGHT TARGET LOCK");
        mLimelight.setTracking(ETrackingType.NONE);
    }

}
