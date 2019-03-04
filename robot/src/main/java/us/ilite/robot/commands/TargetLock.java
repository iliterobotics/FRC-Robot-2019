package us.ilite.robot.commands;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.flybotix.hfr.codex.Codex;

import us.ilite.common.lib.util.CheesyDriveHelper;
import com.team254.lib.util.DriveSignal;
import com.team254.lib.util.Util;
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
    private static final double kTURN_POWER = 0.2;
    private static final int kAlignCount = 10;
    private static final double kFrictionFeedforward = 0.44 / 12;
    private static final double kTargetAreaScalar = 1.0;

    private Drive mDrive;
    private ITargetDataProvider mCamera;
    // Different throttle providers give us some control over behavior in autonomous
    private IThrottleProvider mTargetSearchThrottleProvider, mTargetLockThrottleProvider;
    private PIDController mPID;
    private ETrackingType mTrackingType;

    private double mAllowableError, mPreviousTime, mOutput = 0.0;

    private boolean mEndOnAlignment = true;
    private int mAlignedCount = 0;
    private boolean mHasAcquiredTarget = false;

    private CheesyDriveHelper mCheesyDriveHelper = new CheesyDriveHelper(SystemSettings.kCheesyDriveGains);

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

        if(mPID != null && currentData != null && currentData.isSet(ETargetingData.tv) && currentData.get(ETargetingData.tx) != null) {
            mHasAcquiredTarget = true;

            //if there is a target in the limelight's fov, lock onto target using feedback loop
            mOutput = mPID.calculate(-1.0 * currentData.get(ETargetingData.tx), pNow - mPreviousTime);
            mOutput = mOutput + (Math.signum(mOutput) * kFrictionFeedforward);

            double throttle = mTargetLockThrottleProvider.getThrottle() * SystemSettings.kSnailModePercentThrottleReduction;

            mDrive.setDriveMessage(getCheesyDrive(throttle, mOutput, currentData));

            SmartDashboard.putNumber("PID Turn Output", mOutput);
            mAlignedCount++;
            if(mEndOnAlignment && Math.abs(currentData.get(ETargetingData.tx)) < mAllowableError && mAlignedCount > kAlignCount) {
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
            mAlignedCount = 0;
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

    /*
    Why CheesyDrive?
    The short answer is that it's a hack. We noticed that we tended to get oscillations that were bigger in amplitude the
    faster we drove forward, which led us to believe the steering response of the PID was having less of an effect on the
    turn of the robot the faster we drove. Cheesy Drive fixes this by scaling steering power by throttle power.
     */
    private DriveMessage getCheesyDrive(double throttle, double turn, Codex<Double, ETargetingData> targetData) {
        boolean isQuickTurn = Math.abs(throttle) < Util.kEpsilon;

        DriveSignal cheesyOutput = mCheesyDriveHelper.cheesyDrive(throttle, turn, true);
        return new DriveMessage(cheesyOutput.getLeft(), cheesyOutput.getRight(), ControlMode.PercentOutput).setNeutralMode(NeutralMode.Brake);
    }

    private DriveMessage getArcadeDrive(double throttle, double turn, Codex<Double, ETargetingData> targetData) {
        mOutput *= targetData.get(ETargetingData.ta) * kTargetAreaScalar;
        return new DriveMessage(throttle + turn, throttle - turn, ControlMode.PercentOutput).setNeutralMode(NeutralMode.Brake);
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