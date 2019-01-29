package us.ilite.robot.commands;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.DemandType;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import com.team254.lib.geometry.Rotation2d;
import com.team254.lib.util.Util;
import us.ilite.common.lib.util.Conversions;
import us.ilite.common.types.drive.EDriveData;
import us.ilite.common.types.sensor.EGyro;
import us.ilite.robot.Data;
import us.ilite.robot.modules.Drive;
import us.ilite.robot.modules.DriveMessage;

/**
 * A drive straight command implementing heading control based on both angular velocity and angle.
 * Acceleration/deceleration can be controlled using either a custom implementation relying on
 * % output or the Talon's motion magic control mode.
 */
public class DriveStraight implements ICommand {

    private final ILog mLog = Logger.createLog(DriveStraight.class);

    private final Drive mDrive;
    private final Data mData;

    private final EHeadingControlMode mHeadingControlMode;
    private final EDriveControlMode mDriveControlMode;

    private double mDistanceToDrive;
    private Rotation2d mTargetHeading;
    private double mDriveOutput = 0.7;
    private double mAllowableDistanceError = 3.0;
    private double mRampDistance = 120.0;

    private double mInitialDistance;

    public DriveStraight(Drive pDrive, Data pData, EHeadingControlMode pHeadingControlMode, EDriveControlMode pDriveControlMode, double pDistanceToDrive) {
        mDrive = pDrive;
        mData = pData;
        mDistanceToDrive = pDistanceToDrive;
        mHeadingControlMode = pHeadingControlMode;
        mDriveControlMode = pDriveControlMode;
    }

    /**
     * Indicates whether we want to use angle or angular velocity to keep straight,
     * and holds a gain for holding heading with that mode.
     */
    public enum EHeadingControlMode {
        // % power per degree error
        GYRO_HEADING(0.03, 2.0),
        // % power per degree/s error
        GYRO_RATE(0.0, 1.0);

        public final double kP;
        public final double kErrorCeiling;

        EHeadingControlMode(double pGain, double pErrorCeiling) {
            kP = pGain;
            kErrorCeiling = pErrorCeiling;
        }
    }

    /**
     * Indicates whether we use velocity control or pure % output for drivebase outputs.
     */
    public enum EDriveControlMode {
        VELOCITY(ControlMode.MotionMagic),
        PERCENT_OUTPUT(ControlMode.PercentOutput);

        public final ControlMode kMotorControlMode;

        EDriveControlMode(ControlMode pMotorControlMode) {
            kMotorControlMode = pMotorControlMode;
        }
    }

    @Override
    public void init(double pNow) {
        mTargetHeading = Rotation2d.fromDegrees(mData.imu.get(EGyro.YAW_DEGREES));
        mInitialDistance = getAverageDriveDistance();
    }

    @Override
    public boolean update(double pNow) {

        DriveMessage driveMessage;
        double angularOutput = 0.0, linearOutput = 0.0;
        double distanceError = mDistanceToDrive - getAverageDistanceTraveled();

        switch(mHeadingControlMode) {
            case GYRO_HEADING:
                // Calculate degrees to get back to initial heading
                Rotation2d headingError = mTargetHeading.rotateBy(Rotation2d.fromDegrees(mData.imu.get(EGyro.YAW_DEGREES)).inverse());
                angularOutput = headingError.getDegrees() * mHeadingControlMode.kP;
                break;
            case GYRO_RATE:
                // Drive yaw rate to 0
                angularOutput = mData.imu.get(EGyro.YAW_RATE_DEGREES) * mHeadingControlMode.kP;
                break;
            default:
                mLog.warn("Hit unexpected heading control state: ", mHeadingControlMode.name());
                break;
        }

        switch(mDriveControlMode) {
            case PERCENT_OUTPUT:
                /*
                This is a proportional gain for distance. We scale the power we have available
                by the ratio of distance_remaining to ramp_distance.
                When distance_remaining > ramp_distance, available_power is scaled by a value > 1,
                which means that the total output is > 1 and thus saturated. If distance_remaining
                is < ramp_distance, the power we add in decreases linearly until distance_remaining
                is 0.
                 */
                double availableRampPower = 1.0 - mDriveOutput;
                double distanceGain =  availableRampPower * 1.0 / mRampDistance;
                linearOutput = mDriveOutput + (distanceGain * distanceError);
                /*
                Limits linear output to the % output remaining after power needed to make turn is
                calculated. This ensures that turn is always able to correct for error since linearOutput
                will never saturate our motor output.
                 */
                // TODO Calculate this off of actual angular output?
                linearOutput = Util.limit(mDriveOutput, 1.0 - (angularOutput * mHeadingControlMode.kP));

                break;
            case VELOCITY:
                // Calculate a distance setpoint
                linearOutput = Conversions.inchesToTicks(mInitialDistance + mDistanceToDrive);

                break;
            default:
                mLog.warn("Hit unexpected drive control state: ", mHeadingControlMode.name());
                break;
        }

        if(Util.epsilonEquals(distanceError, 0.0, mAllowableDistanceError)) {

            // Stop drivebase
            mDrive.setDriveMessage(DriveMessage.kNeutral);

            return true;
        } else {
            /*
            Apply heading correction as an arbitrary output (not dependent on control mode). This means
            that we can use the same math for heading correction no matter which motor control mode we
            are using.
            */
            driveMessage = new DriveMessage(linearOutput, linearOutput, mDriveControlMode.kMotorControlMode);
            driveMessage.setDemand(DemandType.ArbitraryFeedForward, angularOutput, -angularOutput);
            driveMessage.setNeutralMode(NeutralMode.Brake);

            mDrive.setDriveMessage(driveMessage);

            return false;
        }

    }

    @Override
    public void shutdown(double pNow) {

    }

    private double getAverageDriveDistance() {
        return (mData.drive.get(EDriveData.LEFT_POS_INCHES) + mData.drive.get(EDriveData.RIGHT_POS_INCHES)) / 2.0;
    }

    private double getAverageDistanceTraveled() {
        return getAverageDriveDistance() - mInitialDistance;
    }

    public DriveStraight setDistanceToDrive(double pDistanceToDrive) {
        mDistanceToDrive = pDistanceToDrive;
        return this;
    }

    public DriveStraight setTargetHeading(Rotation2d pTargetHeading) {
        mTargetHeading = pTargetHeading;
        return this;
    }

    public DriveStraight setDriveOutput(double pDriveOutput) {
        mDriveOutput = pDriveOutput;
        return this;
    }

}
