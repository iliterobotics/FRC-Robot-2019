package us.ilite.robot.commands;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import us.ilite.common.lib.physics.DriveCharacterization;
import us.ilite.common.lib.util.Conversions;
import us.ilite.common.lib.util.ReflectingCSVWriter;
import us.ilite.robot.modules.Drive;
import us.ilite.robot.modules.DriveMessage;

import java.util.List;

public class CollectAccelerationData implements ICommand {
    private static final double kPower = 0.5;
    private static final double kTotalTime = 2.0; //how long to run the test for
    private final Drive mDrive;

    private final ReflectingCSVWriter<DriveCharacterization.AccelerationDataPoint> mLeftCSVWriter, mRightCSVWriter;
    private final List<DriveCharacterization.AccelerationDataPoint> mLeftAccelerationData, mRightAccelerationData;
    private final boolean mTurn;
    private final boolean mReverse;

    private double mStartTime = 0.0;
    private double mLeftPrevVelocity = 0.0, mRightPrevVelocity = 0.0;
    private double mPrevTime = 0.0;

    /**
     * @param leftData     reference to the list where data points should be stored
     * @param reverse  if true drive in reverse, if false drive normally
     * @param turn     if true turn, if false drive straight
     */
    public CollectAccelerationData(Drive pDriveTrain, List<DriveCharacterization.AccelerationDataPoint> leftData, List<DriveCharacterization.AccelerationDataPoint> rightData, boolean reverse, boolean turn) {
        mDrive = pDriveTrain;
        mLeftAccelerationData = leftData;
        mRightAccelerationData = rightData;
        mReverse = reverse;
        mTurn = turn;
        mLeftCSVWriter = new ReflectingCSVWriter<>("/home/lvuser/LEFT_ACCEL_DATA.csv", DriveCharacterization.AccelerationDataPoint.class);
        mRightCSVWriter = new ReflectingCSVWriter<>("/home/lvuser/RIGHT_ACCEL_DATA.csv", DriveCharacterization.AccelerationDataPoint.class);
    }

    @Override
    public void init(double pNow) {
        DriveMessage driveMessage = new DriveMessage((mReverse ? -1.0 : 1.0) * kPower, (mReverse ? -1.0 : 1.0) * (mTurn ? -1.0 : 1.0) * kPower, ControlMode.PercentOutput);
        driveMessage.setNeutralMode(NeutralMode.Coast);
        mDrive.setDriveMessage(driveMessage);
        mStartTime = pNow;
        mPrevTime = mStartTime;
    }

    @Override
    public boolean update(double pNow) {
        double currentLeftVelocity = Conversions.ticksPer100msToRadiansPerSecond(mDrive.getDriveHardware().getLeftVelTicks());
        double currentRightVelocity = Conversions.ticksPer100msToRadiansPerSecond(mDrive.getDriveHardware().getRightVelTicks());

        double currentTime = pNow;

        //don't calculate acceleration until we've populated prevTime and prevVelocity
        if (mPrevTime == mStartTime) {
            mPrevTime = currentTime;
            mLeftPrevVelocity = currentLeftVelocity;
            mRightPrevVelocity = currentRightVelocity;
            return false;
        }

        double leftAcceleration = (currentLeftVelocity - mLeftPrevVelocity) / (currentTime - mPrevTime);
        double rightAcceleration = (currentRightVelocity - mRightPrevVelocity) / (currentTime - mPrevTime);


        //ignore accelerations that are too small
        if (leftAcceleration < 1E-9) {
            mPrevTime = currentTime;
            mLeftPrevVelocity = currentLeftVelocity;
            return false;
        }

        //ignore accelerations that are too small
        if (rightAcceleration < 1E-9) {
            mPrevTime = currentTime;
            mRightPrevVelocity = currentRightVelocity;
            return false;
        }

        mLeftAccelerationData.add(new DriveCharacterization.AccelerationDataPoint(
                currentLeftVelocity, //convert to radians per second
                kPower * 12.0, //convert to volts
                leftAcceleration
        ));

        mRightAccelerationData.add(new DriveCharacterization.AccelerationDataPoint(
                currentRightVelocity, //convert to radians per second
                kPower * 12.0, //convert to volts
                rightAcceleration
        ));

        mLeftCSVWriter.add(mLeftAccelerationData.get(mLeftAccelerationData.size() - 1));
        mRightCSVWriter.add(mRightAccelerationData.get(mRightAccelerationData.size() - 1));


        mPrevTime = currentTime;
        mLeftPrevVelocity = currentLeftVelocity;
        mRightPrevVelocity = currentRightVelocity;

        if(pNow - mStartTime > kTotalTime) return true;

        return false;
    }

    @Override
    public void shutdown(double pNow) {
        mDrive.setDriveMessage(new DriveMessage(0.0, 0.0, ControlMode.PercentOutput).setNeutralMode(NeutralMode.Coast));
        mLeftCSVWriter.flush();
        mRightCSVWriter.flush();
    }
}
