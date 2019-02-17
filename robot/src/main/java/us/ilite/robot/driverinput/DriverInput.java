package us.ilite.robot.driverinput;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.flybotix.hfr.codex.Codex;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import com.team254.lib.util.Util;
import edu.wpi.first.wpilibj.Joystick;
import us.ilite.common.Data;
import us.ilite.common.config.DriveTeamInputMap;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.types.ETrackingType;
import us.ilite.common.types.input.EInputScale;
import us.ilite.common.types.input.ELogitech310;
import us.ilite.robot.modules.Drive;
import us.ilite.robot.modules.DriveMessage;
import us.ilite.robot.modules.HatchFlower;
import us.ilite.robot.commands.TargetLock;
import us.ilite.robot.modules.*;
import us.ilite.robot.modules.Module;

public class DriverInput extends Module implements IThrottleProvider, ITurnProvider {

    protected static final double 
    DRIVER_SUB_WARP_AXIS_THRESHOLD = 0.5;
    private ILog mLog = Logger.createLog(DriverInput.class);

    protected final Drive mDrive;
    protected final Elevator mElevator;
    protected final HatchFlower mHatchFlower;
    protected final Superstructure mSuperstructure;
    protected final Limelight mLimelight;

    private boolean mDriverStartedCommands;

    private Joystick mDriverJoystick;
    private Joystick mOperatorJoystick;

    protected Codex<Double, ELogitech310> mDriverInputCodex, mOperatorInputCodex;

    private Data mData;

    public DriverInput(Drive pDrivetrain, Elevator pElevator, HatchFlower pHatchFlower, Superstructure pSuperstructure, Limelight pLimelight, Data pData, boolean pSimulated) {
        this.mDrive = pDrivetrain;
        this.mHatchFlower = pHatchFlower;
        this.mSuperstructure = pSuperstructure;
        this.mLimelight = pLimelight;
        this.mData = pData;
        this.mDriverInputCodex = mData.driverinput;
        this.mOperatorInputCodex = mData.operatorinput;
        this.mElevator = pElevator;
        if(pSimulated) {
            // Use a different joystick library?
            
        } else {
            this.mDriverJoystick = new Joystick(0);
            this.mOperatorJoystick = new Joystick(1);
        }
    }

    public DriverInput(Drive pDrivetrain, Elevator pElevator, HatchFlower pHatchFlower, Superstructure pSuperstructure, Limelight pLimelight, Data pData) {
        this(pDrivetrain, pElevator, pHatchFlower, pSuperstructure, pLimelight, pData, false);
    }

    @Override
    public void modeInit(double pNow) {
    // TODO Auto-generated method stub
        mDriverStartedCommands = false;
    }

    @Override
    public void periodicInput(double pNow) {
        ELogitech310.map(mData.driverinput, mDriverJoystick);
        ELogitech310.map(mData.operatorinput, mOperatorJoystick);
    }

    @Override
    public void update(double pNow) {
        /*
        If we aren't already running commands and the driver is pressing a button that triggers a command,
        set the superstructure command queue based off of buttons
        */
        if(!mDriverStartedCommands && isDriverAllowingTeleopCommands()) {
            mLog.warn("Requesting command start");
            mDriverStartedCommands = true;
            updateVisionCommands();
        /*
        If the driver started the commands that the superstructure is running and then released the button,
        stop running commands.
        */
        } else if(mSuperstructure.isRunningCommands() && mDriverStartedCommands && !isDriverAllowingTeleopCommands()) {
            mLog.warn("Requesting command stop: driver no longer allowing commands");
            mDriverStartedCommands = false;
            mSuperstructure.stopRunningCommands();
        } else if(mSuperstructure.isRunningCommands() && isAutoOverridePressed()) {
            mLog.warn("Requesting command stop: override pressed");
            mSuperstructure.stopRunningCommands();
            mDriverStartedCommands = false;
        } else if(!mSuperstructure.isRunningCommands()) {
            mDriverStartedCommands = false;
        }


            // Teleop control
        if (!mSuperstructure.isRunningCommands()) {
            updateDriveTrain();
            updateElevator();
        } 

    }

    private void updateHatchFlower() {
        if(mData.driverinput.isSet(DriveTeamInputMap.DRIVER_HATCH_FLOWER_CAPTURE_BTN)) {
            mHatchFlower.captureHatch();
        }
        else if(mData.driverinput.isSet(DriveTeamInputMap.DRIVER_HATCH_FLOWER_PUSH_BTN)) {
            mHatchFlower.pushHatch();
        }
    }

    private void updateDriveTrain() {
        double rotate = getTurn();
        double throttle = getThrottle();

        //		    throttle = EInputScale.EXPONENTIAL.map(throttle, 2);
        rotate = EInputScale.EXPONENTIAL.map(rotate, 2);
        rotate = Util.limit(rotate, 0.7);

        if (mData.driverinput.isSet(DriveTeamInputMap.DRIVER_SUB_WARP_AXIS) && mData.driverinput.get(DriveTeamInputMap.DRIVER_SUB_WARP_AXIS) > DRIVER_SUB_WARP_AXIS_THRESHOLD) {
            throttle *= SystemSettings.kSnailModePercentThrottleReduction;
            rotate *= SystemSettings.kSnailModePercentRotateReduction;
        }

        DriveMessage driveMessage = DriveMessage.fromThrottleAndTurn(throttle, rotate);
        driveMessage.setNeutralMode(NeutralMode.Brake);
        driveMessage.setControlMode(ControlMode.PercentOutput);

        mDrive.setDriveMessage(driveMessage);
    }

    private void updateElevator() {
        double throttle1 = -mData.operatorinput.get(ELogitech310.LEFT_TRIGGER_AXIS);
        double throttle2 = mData.operatorinput.get(ELogitech310.RIGHT_TRIGGER_AXIS);
        double throttle = throttle1 + throttle2;


         if (mData.operatorinput.isSet(DriveTeamInputMap.MANIPULATOR_BOTTOM_POSITION_ELEVATOR)) {
            mElevator.setDesirecPosition(EElevatorPosition.BOTTOM);
        } else if (mData.operatorinput.isSet(DriveTeamInputMap.MANIPULATOR_MIDDLE_POSITION_ELEVATOR)) {
            mElevator.setDesirecPosition(EElevatorPosition.MIDDLE);
        } else if (mData.operatorinput.isSet(DriveTeamInputMap.MANIPULATOR_TOP_POSITION_ELEVATOR)) {
            mElevator.setDesirecPosition(EElevatorPosition.TOP);
        } else if (mData.driverinput.isSet(DriveTeamInputMap.MANIPULATOR_CONTROL_ELEVATOR)) {
             double power = mData.operatorinput.get(DriveTeamInputMap.MANIPULATOR_CONTROL_ELEVATOR);
             mElevator.setDesiredPower(throttle);
         } else {
            mElevator.setDesiredPower(0d);
        }
    }
      
    private void updateSplitTriggerAxisFlip() {

        double rotate = mDriverInputCodex.get(DriveTeamInputMap.DRIVER_TURN_AXIS);
        double throttle = -mDriverInputCodex.get(DriveTeamInputMap.DRIVER_THROTTLE_AXIS);

        if(mDriverInputCodex.get(ELogitech310.RIGHT_TRIGGER_AXIS) > 0.3) {
            rotate = rotate;
            throttle = throttle;
        } else if(mDriverInputCodex.get(ELogitech310.LEFT_TRIGGER_AXIS) > 0.3) {
            throttle = -throttle;
            rotate = rotate;
        }

        rotate = Util.limit(rotate, SystemSettings.kDriverInputTurnMaxMagnitude);

		// throttle = EInputScale.EXPONENTIAL.map(throttle, 2);
        // rotate = Util.limit(rotate, 0.7);

        // if (mDriverInputCodex.get(DriveTeamInputMap.DRIVER_SUB_WARP_AXIS) > DRIVER_SUB_WARP_AXIS_THRESHOLD) {
        //     throttle *= SystemSettings.kSnailModePercentThrottleReduction;
        //     rotate *= SystemSettings.kSnailModePercentRotateReduction;
        // }

        DriveMessage driveMessage = DriveMessage.fromThrottleAndTurn(throttle, rotate);
        driveMessage.setNeutralMode(NeutralMode.Brake);
        driveMessage.setControlMode(ControlMode.PercentOutput);

        mDrive.setDriveMessage(driveMessage);

    }

    /**
     * Commands the superstructure to start vision tracking depending on
     * button presses.
     */
    protected void updateVisionCommands() {

        ETrackingType trackingType = null;
        SystemSettings.VisionTarget visionTarget = null;

        // Switch the limelight to a pipeline and track
        if(mDriverInputCodex.isSet(DriveTeamInputMap.DRIVER_TRACK_TARGET_BTN)) {
            trackingType = ETrackingType.TARGET_LEFT;
            // TODO Determine which target height we're using
            visionTarget = SystemSettings.VisionTarget.HatchPort;
        } else if(mDriverInputCodex.isSet(DriveTeamInputMap.DRIVER_TRACK_CARGO_BTN)) {
            trackingType = ETrackingType.CARGO_LEFT;
            visionTarget = SystemSettings.VisionTarget.CargoHeight;
        } else if(mDriverInputCodex.isSet(DriveTeamInputMap.DRIVER_TRACK_HATCH_BTN)) {
            trackingType = ETrackingType.LINE_LEFT;
            visionTarget = SystemSettings.VisionTarget.Ground;
        }

        // If the driver selected a tracking enum and we won't go out of bounds
        if(trackingType != null && trackingType.ordinal() < ETrackingType.values().length - 1) {
            int trackingTypeOrdinal = trackingType.ordinal();
            if(mDriverInputCodex.isSet(DriveTeamInputMap.DRIVER_NUDGE_SEEK_LEFT)) {
                // If driver wants to seek left, we don't need to change anything
                trackingType = ETrackingType.values()[trackingTypeOrdinal];
            } else if(mDriverInputCodex.isSet(DriveTeamInputMap.DRIVER_NUDGE_SEEK_RIGHT)) {
                // If driver wants to seek right, switch from "_LEFT" enum to "_RIGHT" enum
                trackingType = ETrackingType.values()[trackingTypeOrdinal + 1];
            }
            
            mSuperstructure.stopRunningCommands();
            mLimelight.setVisionTarget(visionTarget);
            mLimelight.setPipeline(trackingType.getPipeline());
            mSuperstructure.startCommands(new TargetLock(mDrive, 3, trackingType, mLimelight, this, false));
            
        }
    }

    public boolean isDriverAllowingTeleopCommands() {
        boolean runCommands = false;
        for(ELogitech310 l : SystemSettings.kTeleopCommandTriggers) {
            if(mDriverInputCodex.isSet(l)) {
                runCommands = true;
            }
        }
        return runCommands;
    }

    public boolean isAutoOverridePressed() {
        boolean autonOverride = false;
        for(ELogitech310 l : SystemSettings.kAutonOverrideTriggers) {
            if(mDriverInputCodex.isSet(l) && mDriverInputCodex.get(l) > SystemSettings.kAutonOverrideAxisThreshold) {
                autonOverride = true;
            }
        }
        return autonOverride;
    }

    @Override
    public void shutdown(double pNow) {

    }

    @Override
    public double getThrottle() {
        if(mData.driverinput.isSet(DriveTeamInputMap.DRIVER_THROTTLE_AXIS)) {
            return -mData.driverinput.get(DriveTeamInputMap.DRIVER_THROTTLE_AXIS);
        } else {
            return 0.0;
        }
    }

    @Override
    public double getTurn() {
        if(mData.driverinput.isSet(DriveTeamInputMap.DRIVER_TURN_AXIS)) {
            return mData.driverinput.get(DriveTeamInputMap.DRIVER_TURN_AXIS);
        } else {
            return 0.0;
        }
    }
}
