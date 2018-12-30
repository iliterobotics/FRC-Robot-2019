package us.ilite.robot.commands;

import us.ilite.lib.drivers.IMU;
import us.ilite.lib.drivers.Pigeon;
import us.ilite.robot.modules.Drive;
import us.ilite.robot.modules.DriveMessage;
import us.ilite.common.lib.geometry.Rotation2d;
import us.ilite.common.types.sensor.EGyro;
import us.ilite.robot.Data;
// import us.ilite.robot.modules.drivetrain.DrivetrainMode;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.ControlMode;

public class TurnToDegree implements ICommand {
  private Drive mDrive;
  private Pigeon mPigeon;
  
  private static final int kMIN_ALIGNED_COUNT = 5;
  private static final double kTIMEOUT = 1.5;
  private static final double kP = 0.0121;
  private static final double kI = 0.0001;
  private static final double kD = 0.08;
  private static final double kMIN_POWER = 0.05;
  private static final double kMAX_POWER = 1.0;
  
  private Rotation2d mInitialYaw, mTurnAngle, mTargetYaw;
  private Rotation2d mError, mLastError, mTotalError;
  private double mLeftPower, mRightPower, mOutput = 0.0;
  private double mStartTime;
  private int mAlignedCount;
  private final double mAllowableError;
  public Data mData;
  
  public TurnToDegree(Drive pDrive, Pigeon pPigeon, Rotation2d pTurnAngle, double pAllowableError, Data pData) {
    this.mDrive = pDrive;
    this.mPigeon = pPigeon;
    
    this.mTurnAngle = pTurnAngle;
    this.mAllowableError = pAllowableError;

    this.mAlignedCount = 0;

    this.mData = pData;
  }

  @Override
  public void init(double pNow) {
    mStartTime = pNow;
    
    mInitialYaw = getYaw();
    mTargetYaw = mInitialYaw.rotateBy( mTurnAngle );
    
    this.mError = getError();
    this.mLastError = mError; // Calculate the initial error value
    this.mTotalError = mError;
  }

  public boolean update(double pNow) {
    
    mError = getError(); // Update error value
    this.mTotalError = this.mTotalError.rotateBy(this.mError);; // Update running error total
    
    mOutput = ((kP * mError.getDegrees()) + (kI * mTotalError.getDegrees()) + (kD * (mError.getDegrees() - mLastError.getDegrees())));
    
    int scalar = mOutput > 0 ? 1 : -1;
    if(Math.abs(mOutput) <= kMIN_POWER) mOutput = kMIN_POWER * scalar;
    
    if(Math.abs(mOutput) >= kMAX_POWER) mOutput = kMAX_POWER * scalar;
    
    mLeftPower = mOutput;
    mRightPower = -mOutput;
    mLastError = mError;

    if ((Math.abs(mError.getDegrees()) <= Math.abs(mAllowableError))) {
      mDrive.zero();
//      mAlignedCount++;
      return true;
    }
    if(pNow - mStartTime > kTIMEOUT) {
//      mDrivetrain.setDriveMessage(new DrivetrainMessage(0.0, 0.0, DrivetrainMode.PercentOutput, NeutralMode.Brake));
      return true;
    }
    mDrive.setDriveMessage(new DriveMessage(mLeftPower, mRightPower, ControlMode.PercentOutput).setNeutralMode(NeutralMode.Brake));
//    if(mAlignedCount >= kMIN_ALIGNED_COUNT) {
//      mDrivetrain.setDriveMessage(new DrivetrainMessage(0.0, 0.0, DrivetrainMode.PercentOutput, NeutralMode.Brake));
//      return true;
//    }
    
    System.out.printf("Target: %s Yaw: %s\n", mTargetYaw, getYaw());
    return false;
  }

  public Rotation2d getError() {
    return mTargetYaw.rotateBy(getYaw());
  }
  
  private Rotation2d getYaw() {
    return Rotation2d.fromDegrees(mData.imu.get(EGyro.YAW_DEGREES));
  }
  
  @Override
  public void shutdown(double pNow) {
    
  }
 
}