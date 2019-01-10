package us.ilite.robot.commands;

import us.ilite.lib.drivers.IMU;
import us.ilite.lib.drivers.Pigeon;
import us.ilite.robot.modules.Drive;
import us.ilite.robot.modules.DriveMessage;
import us.ilite.common.types.sensor.EGyro;
import us.ilite.robot.Data;
import control.PIDController;
// import us.ilite.robot.modules.drivetrain.DrivetrainMode;

import com.ctre.phoenix.motorcontrol.NeutralMode;

import com.ctre.phoenix.motorcontrol.ControlMode;

public class TurnToDegree implements ICommand {
  private Drive mDrive;
  private Pigeon mPigeon;
  
  private static final int kMIN_ALIGNED_COUNT = 5;
  private static final double kTIMEOUT = 1.5;
  private static final double kP = 0.03;
  private static final double kI = 0.0;
  private static final double kD = 0.0;
  private static final double kMIN_POWER = 0.0; //0.066666667
  private static final double kMAX_POWER = 1.0;
  
  private Rotation2d mInitialYaw, mTurnAngle, mTargetYaw;
  private double mError, mLastError, mTotalError;
  private double mLeftPower, mRightPower, mOutput = 0.0;
  private double mStartTime;
  private int mAlignedCount;
  private PIDController pid;
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

    pid = new PIDController(kP, kI, kD);
    pid.setContinuous();
    pid.setOutputRange(kMIN_POWER, kMAX_POWER);
    pid.setSetpoint(getYaw().rotateBy(mTargetYaw).getDegrees());
    pid.setInputRange( -180, 180 );
    pid.setOutputRange( -1, 1 );
    mInitialYaw = getYaw();
    mTargetYaw = mInitialYaw.rotateBy( mTurnAngle );
    
    this.mError = pid.getError();
    this.mLastError = mError; // Calculate the initial error value
    this.mTotalError = mError;
  }

  public boolean update(double pNow) {
    mError = pid.getError();
    this.mTotalError += this.mError;
    mOutput = pid.calculate(getYaw().getDegrees(), pNow);
    mLeftPower = mOutput;
    mRightPower = -mOutput;
    mLastError = mError;
    if ((Math.abs(mError) <= Math.abs(mAllowableError))) {
      mDrive.zero();
      return true;
    }
    if(pNow - mStartTime > kTIMEOUT) {
      return true;
    }
    mDrive.setDriveMessage(new DriveMessage(mLeftPower, mRightPower, ControlMode.PercentOutput).setNeutralMode(NeutralMode.Brake));
    System.out.printf("Target: %s Yaw: %s\n", mTargetYaw, getYaw());
    return false;
  }
  
  private Rotation2d getYaw() {
    return Rotation2d.fromDegrees(mData.imu.get(EGyro.YAW_DEGREES));
  }
  
  @Override
  public void shutdown(double pNow) {
    
  }
 
}