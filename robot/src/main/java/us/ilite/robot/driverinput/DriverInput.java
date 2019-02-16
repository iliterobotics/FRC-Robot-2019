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
import us.ilite.robot.commands.*;
import us.ilite.robot.modules.*;
import us.ilite.robot.modules.Module;

public class DriverInput extends Module {

    protected static final double 
    DRIVER_SUB_WARP_AXIS_THRESHOLD = 0.5;
    private ILog mLog = Logger.createLog(DriverInput.class);

    protected final Drive driveTrain;
    protected final Elevator mElevator;
    protected final Intake mIntake;
    protected final HatchFlower mHatchFlower;
    protected final CargoSpit mCargoSpit;
    protected final Superstructure mSuperstructure;

    private boolean mDriverStartedCommands;
    private boolean mIsCargo = false;

    private Joystick mDriverJoystick;
    private Joystick mOperatorJoystick;

    protected Codex<Double, ELogitech310> mDriverInputCodex, mOperatorInputCodex;

    private Data mData;

    public DriverInput(Drive pDrivetrain, Elevator pElevator, Intake pIntake, HatchFlower pHatchFlower, CargoSpit pCargoSpit, Superstructure pSuperstructure, Data pData, boolean pSimulated) {
        this.driveTrain = pDrivetrain;
        this.mElevator = pElevator;
        this.mIntake = pIntake;
        this.mHatchFlower = pHatchFlower;
        this.mCargoSpit = pCargoSpit;
        this.mSuperstructure = pSuperstructure;
        this.mData = pData;
        this.mDriverInputCodex = mData.driverinput;
        this.mOperatorInputCodex = mData.operatorinput;
        if(pSimulated) {
            // Use a different joystick library?
            
        } else {
            this.mDriverJoystick = new Joystick(0);
            this.mOperatorJoystick = new Joystick(1);
        }
    }

    public DriverInput(Drive pDriveTrain, Elevator pElevator, Intake pIntake, HatchFlower pHatchFlower, CargoSpit pCargoSpit, Superstructure pSuperstructure, Data pData) {
        this(pDriveTrain, pElevator, pIntake, pHatchFlower, pCargoSpit, pSuperstructure, pData, false);
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
        }

        // Teleop control
        if (!mSuperstructure.isRunningCommands()) {

            updateDriveTrain();

            if(mOperatorInputCodex.isSet(DriveTeamInputMap.MANIPULATOR_CARGO_SELECT)) {
                mIsCargo = true;
            } else if(mOperatorInputCodex.isSet(DriveTeamInputMap.MANIPULATOR_HATCH_SELECT)) {
                mIsCargo = false;
            }

            updateHatchGrabber();
            updateElevator();
            updateIntake();

        } 

    }

    private void updateIntake() {

        if(mOperatorInputCodex.get(DriveTeamInputMap.MANIPULATOR_INTAKE_GROUND_AXIS) > 0.5) {
            if(mIsCargo) {
                /*
                Tell both the intake and the cargo spit to start intaking.
                We expect the cargo spit to stop automatically.
                 */
                mIntake.setIntakingCargo();
                mCargoSpit.setIntaking();
            } else {
                /*
                Reset the hatch grabber so it's ready to receive another hatch and tell the intake to start intaking.
                We intake to stop automatically, or when we release the intake button.
                 */
                mHatchFlower.pushHatch();
                mIntake.setIntakingHatch();
            }
        } else if(mOperatorInputCodex.get(DriveTeamInputMap.MANIPULATOR_SCORE) > 0.5) {
            // If the intake is handing off or stowed, disable these controls
            if(mIntake.isAtPosition(Intake.EWristPosition.STOWED) || mIntake.isAtPosition(Intake.EWristPosition.HANDOFF)) {
                if(mIsCargo) {
                    mCargoSpit.setOuttaking();

                } else {
                    mHatchFlower.pushHatch();
                }
            } else {
                // If the intake is on the ground, outtake with the intake instead of scoring mechanisms
                mIntake.setOuttaking();
            }
        } else {
            // If the intake button is released, stop everything.
            mCargoSpit.stop();
            mIntake.stop();
        }

        if(mOperatorInputCodex.isSet(DriveTeamInputMap.MANIPULATOR_HANDOFF) /* || mIntake.hasHatch() */) {
            mSuperstructure.startCommands(new HandoffHatch(mElevator, mIntake, mHatchFlower));
        }

    }

    private void updateHatchGrabber() {

        if(mIsCargo) {
            mHatchFlower.setFlowerExtended(HatchFlower.ExtensionState.UP);
        } else {
            mHatchFlower.setFlowerExtended(HatchFlower.ExtensionState.DOWN);
        }

    }

    private void updateElevator() {
        EElevatorPosition desiredPosition = null;

        if(mOperatorInputCodex.isSet(DriveTeamInputMap.MANIPULATOR_GROUND_POSITION_ELEVATOR)) {
            desiredPosition = EElevatorPosition.GROUND;
        } else if(mOperatorInputCodex.isSet(DriveTeamInputMap.MANIPULATOR_BOTTOM_POSITION_ELEVATOR)) {
            desiredPosition = mIsCargo ? EElevatorPosition.CARGO_BOTTOM : EElevatorPosition.HATCH_BOTTOM;
        } else if(mOperatorInputCodex.isSet(DriveTeamInputMap.MANIPULATOR_MIDDLE_POSITION_ELEVATOR)) {
            desiredPosition = mIsCargo ? EElevatorPosition.CARGO_MIDDLE : EElevatorPosition.HATCH_MIDDLE;
        } else if(mOperatorInputCodex.isSet(DriveTeamInputMap.MANIPULATOR_TOP_POSITION_ELEVATOR)) {
            desiredPosition = mIsCargo ? EElevatorPosition.CARGO_TOP : EElevatorPosition.HATCH_TOP;
        } else {
            mElevator.setDesiredPower(0.0);
        }

        if(desiredPosition != null) {
            mElevator.setDesiredPosition(desiredPosition);
        }

    }

    private void updateDriveTrain() {
            double rotate = mData.driverinput.get(DriveTeamInputMap.DRIVER_TURN_AXIS);
            double throttle = -mData.driverinput.get(DriveTeamInputMap.DRIVER_THROTTLE_AXIS);

            //		    throttle = EInputScale.EXPONENTIAL.map(throttle, 2);
            rotate = EInputScale.EXPONENTIAL.map(rotate, 2);
            rotate = Util.limit(rotate, 0.7);


            if (mData.driverinput.get(DriveTeamInputMap.DRIVER_SUB_WARP_AXIS) > DRIVER_SUB_WARP_AXIS_THRESHOLD) {
                throttle *= SystemSettings.kSnailModePercentThrottleReduction;
                rotate *= SystemSettings.kSnailModePercentRotateReduction;
            }

            DriveMessage driveMessage = DriveMessage.fromThrottleAndTurn(throttle, rotate);
            driveMessage.setNeutralMode(NeutralMode.Brake);
            driveMessage.setControlMode(ControlMode.PercentOutput);

            driveTrain.setDriveMessage(driveMessage);
    }

//    private void updateElevator() {
//        double throttle1 = -mData.operatorinput.get(ELogitech310.LEFT_TRIGGER_AXIS);
//        double throttle2 = mData.operatorinput.get(ELogitech310.RIGHT_TRIGGER_AXIS);
//        double throttle = throttle1 + throttle2;
//
//
//        if (mData.operatorinput.isSet(DriveTeamInputMap.MANIPULATOR_BOTTOM_POSITION_ELEVATOR)) {
//            mElevator.setDesiredPosition(EElevatorPosition.HATCH_BOTTOM);
//        } else if (mData.operatorinput.isSet(DriveTeamInputMap.MANIPULATOR_MIDDLE_POSITION_ELEVATOR)) {
//            mElevator.setDesiredPosition(EElevatorPosition.HATCH_MIDDLE);
//        } else if (mData.operatorinput.isSet(DriveTeamInputMap.MANIPULATOR_TOP_POSITION_ELEVATOR)) {
//            mElevator.setDesiredPosition(EElevatorPosition.TOP);
//        } else if (mData.driverinput.isSet(DriveTeamInputMap.MANIPULATOR_CONTROL_ELEVATOR)) {
//            double power = mData.operatorinput.get(DriveTeamInputMap.MANIPULATOR_CONTROL_ELEVATOR);
//            mElevator.setDesiredPower(throttle);
//        } else {
//            mElevator.setDesiredPower(0d);
//        }
//    }
      
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

        driveTrain.setDriveMessage(driveMessage);

    }

    /**
     * Commands the superstructure to start vision tracking depending on
     * button presses.
     */
    protected void updateVisionCommands() {

        ETrackingType trackingType = null;
        // Switch the limelight to a pipeline and track
        if(mDriverInputCodex.isSet(DriveTeamInputMap.DRIVER_TRACK_TARGET_BTN)) {
            trackingType = ETrackingType.TARGET_LEFT;
        } else if(mDriverInputCodex.isSet(DriveTeamInputMap.DRIVER_TRACK_CARGO_BTN)) {
            trackingType = ETrackingType.CARGO_LEFT;
        } else if(mDriverInputCodex.isSet(DriveTeamInputMap.DRIVER_TRACK_HATCH_BTN)) {
            trackingType = ETrackingType.HATCH_LEFT;
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
            mSuperstructure.startCommands(new Delay(30)); // Placeholder
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
// TODO Auto-generated method stub

    }

}
