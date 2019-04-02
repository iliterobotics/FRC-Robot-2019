package us.ilite.robot.commands;

import com.flybotix.hfr.codex.Codex;

import us.ilite.common.Data;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.types.ETargetingData;
import us.ilite.common.types.ETrackingType;
import us.ilite.lib.drivers.ECommonControlMode;
import us.ilite.lib.drivers.ECommonNeutralMode;
import us.ilite.robot.modules.Drive;
import us.ilite.robot.modules.DriveMessage;
import us.ilite.robot.modules.IThrottleProvider;
import us.ilite.robot.modules.targetData.ITargetDataProvider;

public class TargetLock implements ICommand {

    private static final double kTURN_POWER = 0.2;
    private static final int kAlignCount = 10;
    private static final double kTargetAreaScalar = 1.0;

    private Drive mDrive;
    private ITargetDataProvider mCamera;
    // Different throttle providers give us some control over behavior in autonomous
    private IThrottleProvider mTargetSearchThrottleProvider, mTargetLockThrottleProvider;
    private ETrackingType mTrackingType;

    private double mAllowableError, mPreviousTime, mOutput = 0.0;

    private boolean mEndOnAlignment = true;
    private int mAlignedCount = 0;
    private int mNoTargetCount = 0;
    private boolean mHasAcquiredTarget = false;

    public TargetLock(Drive pDrive, double pAllowableError, ETrackingType pTrackingType, ITargetDataProvider pCamera, IThrottleProvider pThrottleProvider) {
        this(pDrive, pAllowableError, pTrackingType, pCamera, pThrottleProvider, true);
    }

    public TargetLock(Drive pDrive, double pAllowableError, ETrackingType pTrackingType, ITargetDataProvider pCamera, IThrottleProvider pThrottleProvider, boolean pEndOnAlignment) {
        this.mDrive = pDrive;
        this.mAllowableError = pAllowableError;
        this.mTrackingType = pTrackingType;
        this.mCamera = pCamera;
        this.mTargetSearchThrottleProvider = pThrottleProvider;
        this.mTargetLockThrottleProvider = pThrottleProvider;
        this.mEndOnAlignment = pEndOnAlignment;
    }

    @Override
    public void init(double pNow) {
        System.out.println("++++++++++++++++++++++++++TARGET LOCKING++++++++++++++++++++++++++++++++++++\n\n\n\n");

        mHasAcquiredTarget = false;
        mAlignedCount = 0;

        mDrive.setTargetAngleLock();
        mDrive.setTargetTrackingThrottle(0);

        this.mPreviousTime = pNow;
    }

    @Override
    public boolean update(double pNow) {
        Codex<Double, ETargetingData> currentData = mCamera.getTargetingData();
        Data.kSmartDashboard.getEntry("Has Acquired Target").setBoolean(mHasAcquiredTarget);

        mDrive.setTargetTrackingThrottle(mTargetLockThrottleProvider.getThrottle() * SystemSettings.kTargetLockThrottleReduction);

        if(currentData != null && currentData.isSet(ETargetingData.tv) && currentData.get(ETargetingData.tx) != null) {
            mHasAcquiredTarget = true;

            mAlignedCount++;
            if(mEndOnAlignment && Math.abs(currentData.get(ETargetingData.tx)) < mAllowableError && mAlignedCount > kAlignCount) {
                System.out.println("FINISHED");
                // Zero drive outputs in shutdown()
                return true;
            }

        // If we've already seen the target and lose tracking, exit.
        } else if(!currentData.isSet(ETargetingData.tv)) {
            mNoTargetCount++;
            if(mNoTargetCount >= SystemSettings.kTargetAngleLockLostTargetThreshold) {
                return true;
            }
        }
//        if(!mHasAcquiredTarget){
//            System.out.println("OPEN LOOP");
//            mAlignedCount = 0;
//            //if there is no target in the limelight's pov, continue turning in direction specified by SearchDirection
//            mDrive.setDriveMessage(
//                new DriveMessage(
//                    mTargetSearchThrottleProvider.getThrottle() + (mTrackingType.getTurnScalar() * kTURN_POWER),
//                    mTargetSearchThrottleProvider.getThrottle() + (mTrackingType.getTurnScalar() * -kTURN_POWER),
//                    ECommonControlMode.PERCENT_OUTPUT
//                ).setNeutralMode(ECommonNeutralMode.BRAKE)
//            );
//
//        }

        mPreviousTime = pNow;
        
         //command has not completed
        return false;                                                      
    }

    @Override
    public void shutdown(double pNow) {
        mDrive.setNormal();
        mDrive.setDriveMessage(DriveMessage.kNeutral);
    }

    public TargetLock setTargetLockThrottleProvider(IThrottleProvider pThrottleProvider) {
        this.mTargetLockThrottleProvider = pThrottleProvider;
        return this;
    }

    public TargetLock setTargetSearchThrottleProvider(IThrottleProvider pThrottleProvider) {
        this.mTargetSearchThrottleProvider = pThrottleProvider;
        return this;
    }

}