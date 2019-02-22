package us.ilite.robot.commands;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.flybotix.hfr.codex.Codex;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.lib.control.PIDController;
import us.ilite.common.types.ETargetingData;
import us.ilite.common.types.ETrackingType;
import us.ilite.robot.modules.Drive;
import us.ilite.robot.modules.DriveMessage;
import us.ilite.robot.modules.IThrottleProvider;
import us.ilite.robot.modules.targetData.ITargetDataProvider;

public class TargetLock implements ICommand {

    private static final double kMIN_POWER = -0.5;
    private static final double kMAX_POWER = 0.5;
    private static final double kMIN_INPUT = -27;
    private static final double kMAX_INPUT = 27;
    private static final double kTURN_POWER = 0.3;
    private static final double kFrictionFeedforward = 0.9 / 12;

    private Drive mDrive;
    private ITargetDataProvider mCamera;
    // Different throttle providers give us some control over behavior in autonomous
    private IThrottleProvider mTargetSearchThrottleProvider, mTargetLockThrottleProvider;
    private PIDController mPID;
    private ETrackingType mTrackingType;

    private double mAllowableError, mPreviousTime, mOutput = 0.0;

    private boolean mEndOnAlignment = true;
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
        mPID = new PIDController(SystemSettings.kTargetAngleLockGains, kMIN_INPUT, kMAX_INPUT, SystemSettings.kControlLoopPeriod);
        mPID.setOutputRange(kMIN_POWER, kMAX_POWER);
        mPID.setSetpoint(0);
        mPID.reset();
        SmartDashboard.putBoolean("Initializing Command", true);

        this.mPreviousTime = pNow;
    }

    @Override
    public boolean update(double pNow) {
        SmartDashboard.putBoolean("Initializing Command", false);
        Codex<Double, ETargetingData> currentData = mCamera.getTargetingData();
        System.out.println("LOCKING " + currentData.get(ETargetingData.tx));

        if(currentData.isSet(ETargetingData.tv)) {
            mHasAcquiredTarget = true;
//            System.out.println("USING PID");
            //if there is a target in the limelight's pov, lock onto target using feedback loop
            mOutput = -1 * mPID.calculate(currentData.get(ETargetingData.tx), pNow - mPreviousTime) + kFrictionFeedforward;
            mDrive.setDriveMessage(new DriveMessage(mTargetLockThrottleProvider.getThrottle() + mOutput, mTargetLockThrottleProvider.getThrottle() - mOutput, ControlMode.PercentOutput).setNeutralMode(NeutralMode.Brake));
            SmartDashboard.putNumber("PID Turn Output", mOutput);

            if(mEndOnAlignment && Math.abs(currentData.get(ETargetingData.tx)) < mAllowableError) {
                //if x offset from crosshair is within acceptable error, command TargetLock is completed
                System.out.println("FINISHED");
                mDrive.setDriveMessage(DriveMessage.kNeutral);
                return true;
            }
            // If we've already seen the target and lose tracking, exit.
        } else if(mHasAcquiredTarget && !currentData.isSet(ETargetingData.tv)) {
            mDrive.setDriveMessage(DriveMessage.kNeutral);
            return true;
        } if(!mHasAcquiredTarget){
            System.out.println("OPEN LOOP");
            //if there is no target in the limelight's pov, continue turning in direction specified by SearchDirection
            mDrive.setDriveMessage(
                new DriveMessage(
                    mTargetSearchThrottleProvider.getThrottle() + mTrackingType.getTurnScalar() * kTURN_POWER,
                    mTargetSearchThrottleProvider.getThrottle() + mTrackingType.getTurnScalar() * -kTURN_POWER,
                    ControlMode.PercentOutput
                ).setNeutralMode(NeutralMode.Brake)
            );

        }

        mPreviousTime = pNow;
        
         //command has not completed
        return false;                                                      
    }

    @Override
    public void shutdown(double pNow) {

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