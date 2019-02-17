package us.ilite.robot.commands;

import java.util.Optional;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.flybotix.hfr.codex.Codex;

import us.ilite.common.config.SystemSettings;
import us.ilite.common.lib.control.PIDController;
import us.ilite.common.lib.control.PIDGains;
import us.ilite.common.types.ETargetingData;
import us.ilite.common.types.ETrackingType;
import us.ilite.robot.modules.Drive;
import us.ilite.robot.modules.DriveMessage;
import us.ilite.robot.modules.IThrottleProvider;
import us.ilite.robot.modules.targetData.ITargetDataProvider;
import us.ilite.common.lib.control.PIDGains;

public class TargetLock implements ICommand {

    private static final double kMIN_POWER = -0.5;
    private static final double kMAX_POWER = 0.5;
    private static final double kMIN_INPUT = -27;
    private static final double kMAX_INPUT = 27;
    private static final double kP = 0.02;
    private static final double kI = 0.0;
    private static final double kD = 0.0;
    private static final double kTURN_POWER = 0.6;
    private static final double kFrictionFeedforward = 0.9 / 12;

    private Drive mDrive;
    private ITargetDataProvider mCamera;
    private IThrottleProvider mThrottleProvider;
    private PIDController mPID = new PIDController(SystemSettings.kTargetLockPIDGains, kMIN_INPUT, kMAX_INPUT, SystemSettings.kControlLoopPeriod);
    private ETrackingType mTrackingType;

    private double mAllowableError, mPreviousTime, mOutput = 0.0;

    private boolean mIsAllowedToFinish = true;

    public TargetLock(Drive pDrive, double pAllowableError, ETrackingType pTrackingType, ITargetDataProvider pCamera, IThrottleProvider pThrottleProvider) {
        this(pDrive, pAllowableError, pTrackingType, pCamera, pThrottleProvider, true);
    }

    public TargetLock(Drive pDrive, double pAllowableError, ETrackingType pTrackingType, ITargetDataProvider pCamera, IThrottleProvider pThrottleProvider, boolean pIsAllowedToFinish) {
        this.mDrive = pDrive;
        this.mAllowableError = pAllowableError;
        this.mTrackingType = pTrackingType;
        this.mCamera = pCamera;
        this.mThrottleProvider = pThrottleProvider;
        this.mIsAllowedToFinish = pIsAllowedToFinish;
    }

    @Override
    public void init(double pNow) {
        System.out.println("++++++++++++++++++++++++++TARGET LOCKING++++++++++++++++++++++++++++++++++++");
        mPID.setOutputRange(kMIN_POWER, kMAX_POWER);
        mPID.setSetpoint(0);
        mPID.reset();

        this.mPreviousTime = pNow;
    }

    @Override
    public boolean update(double pNow) {
        Codex<Double, ETargetingData> currentData = mCamera.getTargetingData();
        System.out.println("LOCKING " + currentData.get(ETargetingData.tx));

        if(currentData.isSet(ETargetingData.tv)) {
            System.out.println("USING PID");
            //if there is a target in the limelight's pov, lock onto target using feedback loop
            mOutput = -1 * mPID.calculate(currentData.get(ETargetingData.tx), pNow - mPreviousTime) + kFrictionFeedforward;
            System.out.println(mOutput);
            mDrive.setDriveMessage(new DriveMessage(mThrottleProvider.getThrottle() + mOutput, mThrottleProvider.getThrottle() - mOutput, ControlMode.PercentOutput).setNeutralMode(NeutralMode.Brake));

            if(mIsAllowedToFinish && Math.abs(currentData.get(ETargetingData.tx)) < mAllowableError) {
                //if x offset from crosshair is within acceptable error, command TargetLock is completed
                System.out.println("FINISHED");
                return true;
            }
        } else {
            System.out.println("OPEN LOOP");
            //if there is no target in the limelight's pov, continue turning in direction specified by SearchDirection
            mDrive.setDriveMessage(
                new DriveMessage(
                    mThrottleProvider.getThrottle() + mTrackingType.getTurnScalar() * kTURN_POWER,
                    mThrottleProvider.getThrottle() + mTrackingType.getTurnScalar() * -kTURN_POWER,
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
}