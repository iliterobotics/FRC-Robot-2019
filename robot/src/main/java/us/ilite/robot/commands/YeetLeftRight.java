package us.ilite.robot.commands;

import us.ilite.common.Data;
import us.ilite.common.types.manipulator.EElevator;
import us.ilite.lib.drivers.ECommonControlMode;
import us.ilite.robot.modules.Drive;
import us.ilite.robot.modules.DriveMessage;

public class YeetLeftRight implements ICommand {

    private Data mData;
    private Drive mDrive;

    private double mElevatorTicks;
    private double mVelocityRampRate;
    private double mCruiseVelocity;

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
                mDrive.setDriveMessage(new DriveMessage(0, 0.0, ECommonControlMode.PERCENT_OUTPUT)); // Keep right 0.0

            case RIGHT:
                mDrive.setDriveMessage(new DriveMessage(0.0, 0, ECommonControlMode.PERCENT_OUTPUT)); // Keep left 0.0
        }

        return false;
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
