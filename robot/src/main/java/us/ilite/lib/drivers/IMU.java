package us.ilite.lib.drivers;

import java.util.List;

import com.team254.lib.geometry.Rotation2d;

import org.apache.commons.lang3.ArrayUtils;

import us.ilite.common.lib.util.FilteredAverage;

public abstract  class IMU {
  public enum Axis {
    YAW,
    PITCH,
    ROLL
  }

  //Collision Threshold => Temporary Value
  protected transient double mCollisionThreshold_DeltaG;
  protected transient final FilteredAverage mAccelerationX;
  protected transient final FilteredAverage mAccelerationY;
  protected transient double mJerkX = 0d;
  protected transient double mJerkY = 0d;
  protected double mLastUpdate = 0d;

  public IMU(List<Double>pFilterGains) { 
    this(ArrayUtils.toPrimitive(pFilterGains.toArray(new Double[0])));
  }
  
  public IMU(double[] pFilterGains) {
    mAccelerationX = new FilteredAverage(pFilterGains);
    mAccelerationY = new FilteredAverage(pFilterGains);
  }

  /**
   * Sets the g-force threshold.  This is a tuneable parameter between different robots & years.
   * @param pCollisionThreshold_DeltaG - new g-force parameter
   */
  public final void setCollisionThreshold_DeltaG(double pCollisionThreshold_DeltaG) {
    mCollisionThreshold_DeltaG = pCollisionThreshold_DeltaG;
  }
  
  public double get(Axis pAxis) {
    switch(pAxis) {
    case PITCH:
      return getPitch();
    case ROLL:
      return getRoll();
    case YAW:
    default:
      return getYaw();
    }
  }
  
  /**
   * Pre-populates the filters & calculated values so it's done only once per cycle
   * @param pTimestampNow
   */
  public void update(double pTimestampNow) {
    updateSensorCache(pTimestampNow);
    double currentAccelX = getRawAccelX();
    double currentAccelY = getRawAccelY();
    
    mJerkX = (currentAccelX - mAccelerationX.getAverage()) / (pTimestampNow - mLastUpdate);
    mJerkY = (currentAccelY - mAccelerationY.getAverage()) / (pTimestampNow - mLastUpdate);
    
    mAccelerationX.addNumber(currentAccelX);
    mAccelerationY.addNumber(currentAccelY);
    mLastUpdate = pTimestampNow;
  }
  
  public abstract double getYaw();
  public abstract double getPitch();
  public abstract double getRoll();
  public abstract void zeroAll();
  protected abstract double getRawAccelX();
  protected abstract double getRawAccelY();
  protected abstract void updateSensorCache(double pTimestampNow);

  /**
   * @return whether or not the current values of JerkX and JerkY constitute a collision
   */
  public boolean detectCollision(){
    // Combines both axes to get vector magnitude
    return Math.sqrt(Math.pow(mJerkX, 2) + Math.pow(mJerkY, 2)) >= mCollisionThreshold_DeltaG;
//    return Math.abs(mJerkX) >= mCollisionThreshold_DeltaG || Math.abs(mJerkY) >= mCollisionThreshold_DeltaG;
  }
  
  public double getFilteredAccelX() {
    return mAccelerationX.getAverage();
  }
  
  public double getFilteredAccelY() {
    return mAccelerationY.getAverage();
  }
  
  /**
   * @return the change in acceleration over time
   */
  public double getJerkX() {
    return mJerkX;
  }
  
  /**
   * @return the change in acceleration over time
   */
  public double getJerkY() {
    return mJerkY;
  }
  
  public Rotation2d getHeading() {
    return Rotation2d.fromDegrees(getYaw());
  }
}
