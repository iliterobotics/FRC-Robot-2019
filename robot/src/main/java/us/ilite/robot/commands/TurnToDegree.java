package us.ilite.robot.commands;

import us.ilite.robot.modules.Drive;
import us.ilite.robot.modules.DriveMessage;
import us.ilite.common.types.sensor.EGyro;
import us.ilite.common.Data;
import us.ilite.common.lib.control.PIDController;
import us.ilite.common.config.SystemSettings;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import com.team254.lib.geometry.Rotation2d;

import com.ctre.phoenix.motorcontrol.ControlMode;

public class TurnToDegree implements ICommand {

  private ILog mLogger = Logger.createLog( this.getClass() );

  private static final double kTIMEOUT = 9999.9;
  private static final double kMIN_POWER = 0.0;
  private static final double kMAX_POWER = 0.5;
  private static final int kMIN_ALIGNED_COUNT = 25;
  private final double mAllowableError;

  private double mOutput = 0.0;
  private double mStartTime;
  private int mAlignedCount;

  private Rotation2d mInitialYaw, mTurnAngle, mTargetYaw;
  private PIDController pid;
  private Drive mDrive;
  public Data mData;
  
  public TurnToDegree( Drive pDrive, Rotation2d pTurnAngle, double pAllowableError, Data pData ) {
    this.mDrive = pDrive;
    
    this.mTurnAngle = pTurnAngle;
    this.mAllowableError = pAllowableError;

    this.mData = pData;
  }

  @Override
  public void init( double pNow ) {
    mStartTime = pNow;
    mInitialYaw = getYaw();
    mTargetYaw = mInitialYaw.rotateBy( mTurnAngle );

    // PIDController configuration
    pid = new PIDController( SystemSettings.kPIDGains, -180, 180, SystemSettings.kControlLoopPeriod );
    pid.setContinuous( true );
    pid.setOutputRange( kMIN_POWER, kMAX_POWER );
    pid.setSetpoint( mTargetYaw.getDegrees() );

    mAlignedCount = 0;
  }

  public boolean update( double pNow ) {
    mOutput = pid.calculate( getYaw().getDegrees(), pNow );
    mOutput += Math.signum( mOutput ) * SystemSettings.kPIDGains.kF;

    // Keep track of time on target
    if ( ( Math.abs( pid.getError() ) <= Math.abs( mAllowableError ) ) ) {
     mAlignedCount++;
    } else {
     mAlignedCount = 0;
    }

    // End if on target for 25 counts
    if ( mAlignedCount >= kMIN_ALIGNED_COUNT || pNow - mStartTime > kTIMEOUT ) {
      mDrive.setDriveMessage( new DriveMessage( 0.0, 0.0, ControlMode.PercentOutput ).setNeutralMode( NeutralMode.Brake ) );
      mLogger.info( "Turn finished" );
      return true;
    }

    // Apply output, log, and return false for unfinished
    mDrive.setDriveMessage( new DriveMessage( mOutput, -mOutput, ControlMode.PercentOutput ).setNeutralMode( NeutralMode.Brake ) );
    Data.kSmartDashboard.putDouble( "turn_error", pid.getError() );
    mLogger.info( "Target: " + mTargetYaw + " Yaw: " + getYaw() + "\n" );
    return false;
  }
  
  private Rotation2d getYaw() {
    return Rotation2d.fromDegrees( mData.imu.get( EGyro.YAW_DEGREES ) );
  }
  
  @Override
  public void shutdown( double pNow ) {
    
  }
 
}