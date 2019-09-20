package us.ilite.robot.commands;

import us.ilite.common.Data;
import us.ilite.common.types.drive.EDriveData;
import us.ilite.common.types.manipulator.EElevator;
import us.ilite.lib.drivers.ECommonControlMode;
import us.ilite.robot.modules.Drive;
import us.ilite.robot.modules.DriveMessage;

public class YeetLeftRight implements ICommand {

    private Data mData;
    private Drive mDrive;

    private double mElevatorTicks;
    private double mCurrentVelocity;
    private double mDesiredVelocity;

    //Temporary constant, varies when considering elevator position
    private double kRampRate = .06; //percent per cycle | 0 to .75 in .25 seconds
    private double kCruiseVelocity = 0.75;

    private YeetSide mSideToTurn;

    public enum YeetSide {
        LEFT,
        RIGHT
    }

    public YeetLeftRight(Data pData, Drive pDrive) {
        this.mData = pData;
        this.mDrive = pDrive;
        mElevatorTicks = mData.elevator.get(EElevator.CURRENT_ENCODER_TICKS);
    }

    @Override
    public void init(double pNow) {

    }

    @Override
    public boolean update(double pNow) {

        mElevatorTicks = mData.elevator.get(EElevator.CURRENT_ENCODER_TICKS);

        switch(mSideToTurn) {
            case LEFT:
                mCurrentVelocity = mData.drive.get(EDriveData.LEFT_MESSAGE_OUTPUT);
                mDesiredVelocity = mCurrentVelocity;
                ramp();
                mDrive.setDriveMessage(new DriveMessage(mDesiredVelocity, 0.0, ECommonControlMode.PERCENT_OUTPUT)); // Keep right 0.0

            case RIGHT:
                mCurrentVelocity = mData.drive.get(EDriveData.RIGHT_MESSAGE_OUTPUT);
                mDesiredVelocity = mCurrentVelocity;
                ramp();
                mDrive.setDriveMessage(new DriveMessage(0.0, mDesiredVelocity, ECommonControlMode.PERCENT_OUTPUT)); // Keep left 0.0
        }

        return false;
    }

    public void ramp() {
        if (mCurrentVelocity == kCruiseVelocity || mDesiredVelocity + kRampRate <= kCruiseVelocity) {
            mDesiredVelocity += kRampRate;
        }
    }


    // Output and ramp rate methods based on elevator ticks
//    public void getMaxOutput() {
//
//    }
//
//    public void setRampRate() {
//
//    }

    @Override
    public void shutdown(double pNow) {

    }
}
