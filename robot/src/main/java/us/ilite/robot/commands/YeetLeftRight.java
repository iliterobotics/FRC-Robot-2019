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
    private double mCurrentLeftVelocity;
    private double mDesiredLeftVelocity;
    private double mCurrentRightVelocity;
    private double mDesiredRightVelocity;

    //Temporary constant, varies when considering elevator position
    private double kRampRate = .06; //percent per cycle | 0 to .75 in .25 seconds
    private double kCruiseVelocity = 0.75;

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
                mCurrentLeftVelocity = mData.drive.get(EDriveData.LEFT_MESSAGE_OUTPUT);
                mDesiredLeftVelocity = mCurrentLeftVelocity;
                mDesiredRightVelocity = 0.0;
                ramp();
                break;

            case RIGHT:
                mCurrentRightVelocity = mData.drive.get(EDriveData.RIGHT_MESSAGE_OUTPUT);
                mDesiredRightVelocity = mCurrentRightVelocity;
                mDesiredLeftVelocity = 0.0;
                ramp();
                break;

            default:
                mDesiredLeftVelocity = 0.0;
                mDesiredRightVelocity = 0.0;
                break;
        }

        mDrive.setDriveMessage(new DriveMessage(mDesiredLeftVelocity, mDesiredRightVelocity, ECommonControlMode.PERCENT_OUTPUT));

        return false;
    }

    public void setSideToTurn(EYeetSide pSideToTurn) {
        mSideToTurn = pSideToTurn;
    }

    public void ramp() {
        switch(mSideToTurn){
            case LEFT:
                if (mCurrentLeftVelocity == kCruiseVelocity || mDesiredLeftVelocity + kRampRate <= kCruiseVelocity) {
                    mDesiredLeftVelocity += kRampRate;
                }

            case RIGHT:
                if (mCurrentRightVelocity == kCruiseVelocity || mDesiredRightVelocity + kRampRate <= kCruiseVelocity) {
                    mDesiredRightVelocity += kRampRate;
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
