package us.ilite.robot.commands;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.DemandType;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import com.team254.lib.geometry.Rotation2d;
import com.team254.lib.util.Util;
import us.ilite.common.Data;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.lib.control.PIDController;
import us.ilite.common.lib.util.Conversions;
import us.ilite.common.types.drive.EDriveData;
import us.ilite.common.types.sensor.EGyro;
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

    private final EDriveControlMode mDriveControlMode;

    private double mDistanceToDrive;
    private double mInitialDistance;
    private Rotation2d mTargetHeading;

    private double mDrivePercentOutput = 0.7;
    private double mAllowableDistanceError = 3.0;
    private double mRampDistance = 120.0;
    private double mLastTime = 0.0;
    private PIDController mHeadingController = new PIDController(SystemSettings.kDriveHeadingGains, 0.0, 360.0, SystemSettings.kControlLoopPeriod);

    public DriveStraight(Drive pDrive, Data pData, EDriveControlMode pDriveControlMode, double pDistanceToDrive) {
        mDrive = pDrive;
        mData = pData;
        mDistanceToDrive = pDistanceToDrive;
        mDriveControlMode = pDriveControlMode;
    }

    /**
     * Indicates whether we use velocity control or pure % output for drivebase outputs.
     */
    public enum EDriveControlMode {
        MOTION_MAGIC(ControlMode.MotionMagic),
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
        mLastTime = pNow;

        mHeadingController.setContinuous(true);
        mHeadingController.setOutputRange(-1.0, 1.0);
        mHeadingController.setInputRange(0.0, 360.0);
        mHeadingController.reset();
    }

    @Override
    public boolean update(double pNow) {

        DriveMessage driveMessage;
        double angularOutput = 0.0, linearOutput = 0.0;
        double distanceError = mDistanceToDrive - getAverageDistanceTraveled();

        angularOutput = mHeadingController.calculate(mData.imu.get(EGyro.YAW_DEGREES), pNow - mLastTime);

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
                double availableRampPower = 1.0 - mDrivePercentOutput;
                double distanceGain =  availableRampPower * 1.0 / mRampDistance;
                linearOutput = mDrivePercentOutput + (distanceGain * distanceError);
                /*
                Limits linear output to the % output remaining after power needed to make turn is
                calculated. This ensures that turn is always able to correct for error since linearOutput
                will never saturate our motor output.
                 */
                // TODO Calculate this off of actual angular output?
                linearOutput = Util.limit(linearOutput, 1.0 - angularOutput);

                break;
            case MOTION_MAGIC:
                // TODO Enforce a maximum velocity?
                // Calculate a distance setpoint
                linearOutput = Conversions.inchesToTicks(mInitialDistance + mDistanceToDrive);

                break;
            default:
                mLog.warn("Hit unexpected drive control state: ", mDriveControlMode.name());
                break;
        }

        if(Util.epsilonEquals(distanceError, 0.0, mAllowableDistanceError)) {

            // Stop drivebase
            mDrive.setDriveMessage(DriveMessage.kNeutral);

            mLastTime = pNow;
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

            mLastTime = pNow;
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

    public DriveStraight setDrivePercentOutput(double pDrivePercentOutput) {
        mDrivePercentOutput = pDrivePercentOutput;
        return this;
    }

}
