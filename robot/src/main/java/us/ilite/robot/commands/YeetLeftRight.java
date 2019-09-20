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
    private double mCurrentLeftPercentOutput;
    private double mDesiredLeftPercentOutput;
    private double mCurrentRightPercentOutput;
    private double mDesiredRightPercentOutput;

    //Temporary constant, varies when considering elevator position
    private double kRampRate = .06; //percent per cycle | 0 to .75 in .25 seconds
    private double kCruisePercentOutput = 0.75;

    private EYeetSide mSideToTurn;

    public enum EYeetSide {
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
                mCurrentLeftPercentOutput = mData.drive.get(EDriveData.LEFT_MESSAGE_OUTPUT);
                mDesiredLeftPercentOutput = mCurrentLeftPercentOutput;
                mDesiredRightPercentOutput = 0.0;
                ramp();
                break;

            case RIGHT:
                mCurrentRightPercentOutput = mData.drive.get(EDriveData.RIGHT_MESSAGE_OUTPUT);
                mDesiredRightPercentOutput = mCurrentRightPercentOutput;
                mDesiredLeftPercentOutput = 0.0;
                ramp();
                break;

            default:
                mDesiredLeftPercentOutput = 0.0;
                mDesiredRightPercentOutput = 0.0;
                break;
        }

        mDrive.setDriveMessage(new DriveMessage(mDesiredLeftPercentOutput, mDesiredRightPercentOutput, ECommonControlMode.PERCENT_OUTPUT));

        return false;
    }

    public void setSideToTurn(EYeetSide pSideToTurn) {
        mSideToTurn = pSideToTurn;
    }

    public void ramp() {
        switch(mSideToTurn){
            case LEFT:
                if (mCurrentLeftPercentOutput == kCruisePercentOutput || mDesiredLeftPercentOutput + kRampRate <= kCruisePercentOutput) {
                    mDesiredLeftPercentOutput += kRampRate;
                }

            case RIGHT:
                if (mCurrentRightPercentOutput == kCruisePercentOutput || mDesiredRightPercentOutput + kRampRate <= kCruisePercentOutput) {
                    mDesiredRightPercentOutput += kRampRate;
                }
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
