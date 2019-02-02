package us.ilite.robot.commands;

import java.util.Optional;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.flybotix.hfr.codex.Codex;

import us.ilite.common.lib.control.PIDController;
import us.ilite.common.types.ETargetingData;
import us.ilite.common.types.ETrackingType;
import us.ilite.robot.modules.Drive;
import us.ilite.robot.modules.DriveMessage;
import us.ilite.robot.modules.targetData.ITargetDataProvider;

public class TargetLock implements ICommand {

    private static final double kMIN_POWER = -1;
    private static final double kMAX_POWER = 1;
    private static final double kMIN_INPUT = -27;
    private static final double kMAX_INPUT = 27;
    private static final double kP = 0.017;
    private static final double kI = 0;
    private static final double kD = 0;
    private static final double kTURN_POWER = 0.4;

    private Drive mDrive;
    private ITargetDataProvider mCamera;
    private PIDController mPID = new PIDController(kP, kI, kD);
    private ETrackingType mTrackingType;

    private double mAllowableError, mPreviousTime, mOutput = 0.0;

    public TargetLock(Drive pDrive, double pAllowableError, ETrackingType pTrackingType, ITargetDataProvider pCamera) {
        this.mDrive = pDrive;
        this.mAllowableError = pAllowableError;
        this.mTrackingType = pTrackingType;
        this.mCamera = pCamera;
    }

    @Override
    public void init(double pNow) {
        mPID.setOutputRange(kMIN_POWER, kMAX_POWER);
        mPID.setInputRange(kMIN_INPUT, kMAX_INPUT);
        mPID.setSetpoint(0);
        mPID.reset();

        this.mPreviousTime = pNow;
    }

    @Override
    public boolean update(double pNow) {
        Codex<Double, ETargetingData> currentData = mCamera.getTargetingData();

        // If one data element is set in the codex, they all are
        if(currentData.isSet(ETargetingData.tx)) {
            if(Math.abs(currentData.get(ETargetingData.tx)) < mAllowableError) {
                //if x offset from crosshair is within acceptable error, command TargetLock is completed
                return true;
            }

            if(currentData.isSet(ETargetingData.tv)) {
                //if there is a target in the limelight's pov, lock onto target using feedback loop
                mOutput = mPID.calculate(currentData.get(ETargetingData.tx), pNow - mPreviousTime);
                mDrive.setDriveMessage(new DriveMessage(mOutput, -mOutput, ControlMode.PercentOutput).setNeutralMode(NeutralMode.Brake));
            } else {
                //if there is no target in the limelight's pov, continue turning in direction specified by SearchDirection
                mDrive.setDriveMessage(
                    new DriveMessage(
                        mTrackingType.getTurnScalar() * kTURN_POWER, 
                        mTrackingType.getTurnScalar() * -kTURN_POWER, 
                        ControlMode.PercentOutput
                    ).setNeutralMode(NeutralMode.Brake)
                );
            }
        }

        mPreviousTime = pNow;
        
         //command has not completed
        return false;                                                      
    }

    @Override
    public void shutdown(double pNow) {

    }
}