package us.ilite.robot.commands;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;

import us.ilite.robot.modules.Drive;
import us.ilite.robot.modules.DriveMessage;
import us.ilite.robot.modules.Limelight;
import us.ilite.common.lib.control.PIDController;

public class TargetLock implements ICommand{
    private Drive mDrive;
    private Limelight mLimelight;
    private Limelight.TargetingData mTargetingData;
    private PIDController mPID;
    private SearchDirection mCubeSearchType;

    private static final double kMIN_POWER = -1;
    private static final double kMAX_POWER = 1;
    private static final double kMIN_INPUT = -27;
    private static final double kMAX_INPUT = 27;
    private static final double kP = 0.017;
    private static final double kI = 0;
    private static final double kD = 0;
    private static final double kTURN_POWER = 0.4;

    private double mAllowableError, mPreviousTime, mOutput = 0.0;

    public enum SearchDirection {
		LEFT(-1), RIGHT(1);
		int turnScalar;
		private SearchDirection(int turnScalar) {
			this.turnScalar = turnScalar;
		}
	}

    public TargetLock(Drive pDrive, double pAllowableError, SearchDirection pCubeSearchType) {
        this.mDrive = pDrive;
        this.mAllowableError = pAllowableError;
        this.mCubeSearchType = pCubeSearchType;
    }

    @Override
    public void init(double pNow) {
        mPID = new PIDController(kP, kI, kD);
        mPID.setOutputRange(kMIN_POWER, kMAX_POWER);
        mPID.setInputRange(kMIN_INPUT, kMAX_INPUT);
        mPID.setSetpoint(0);

        this.mPreviousTime = pNow;
    }

    @Override
    public boolean update(double pNow) {
        mTargetingData = mLimelight.new TargetingData();

        if (Math.abs(mTargetingData.tx) < mAllowableError) return true;     //if x offset from crosshair is within acceptable error, command TargetLock is completed

        if (mTargetingData.tv) {                                            //if there is a target in the limelight's pov, lock onto target using feedback loop
            mOutput = mPID.calculate(mTargetingData.tx, pNow - mPreviousTime);

            mDrive.setDriveMessage(new DriveMessage(mOutput, -mOutput, ControlMode.PercentOutput).setNeutralMode(NeutralMode.Brake));
        }   else {                                                          //if there is no target in the limelight's pov, continue turning in direction specified by SearchDirection
            mDrive.setDriveMessage(new DriveMessage(mCubeSearchType.turnScalar * kTURN_POWER, mCubeSearchType.turnScalar * kTURN_POWER, ControlMode.PercentOutput).setNeutralMode(NeutralMode.Brake));
        }
        mPreviousTime = pNow;
        return false;                                                       //command has not completed
    }

    @Override
    public void shutdown(double pNow) {

    }
}