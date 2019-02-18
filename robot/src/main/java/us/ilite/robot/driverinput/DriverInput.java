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
import us.ilite.common.lib.util.RangeScale;
import us.ilite.common.types.ETrackingType;
import us.ilite.common.types.input.EInputScale;
import us.ilite.common.types.input.ELogitech310;
import us.ilite.robot.commands.*;
import us.ilite.robot.modules.*;
import us.ilite.robot.modules.Module;
import us.ilite.robot.modules.Intake.EIntakeState;

public class DriverInput extends Module {

    protected static final double 
    DRIVER_SUB_WARP_AXIS_THRESHOLD = 0.5;
    private ILog mLog = Logger.createLog(DriverInput.class);


    protected final Drive driveTrain;
    protected final Elevator mElevator;
    protected final Intake mIntake;
    protected final HatchFlower mHatchFlower;
    protected final CargoSpit mCargoSpit;
    private final CommandManager mTeleopCommandManager;
    private final CommandManager mAutonomousCommandManager;

    private boolean mIsCargo = false;
    private Joystick mDriverJoystick;
    private Joystick mOperatorJoystick;

    private Arm mArm;
    private RangeScale armJoyStickToAngleScaler = new RangeScale(-1.0, 1.0, SystemSettings.kArmMinAngle, SystemSettings.kArmMaxAngle);

    protected Codex<Double, ELogitech310> mDriverInputCodex, mOperatorInputCodex;

    private Data mData;

    public DriverInput(Drive pDrivetrain, Elevator pElevator, HatchFlower pHatchFlower, CommandManager pAutonomousCommandManager, CommandManager pTeleopCommandManager, CargoSpit pCargoSpit, Intake pIntake, Data pData, boolean pSimulated) {
        this.driveTrain = pDrivetrain;
        this.mHatchFlower = pHatchFlower;
        this.mCargoSpit = pCargoSpit;
        this.mIntake = pIntake;
        this.mElevator = pElevator;
        this.mAutonomousCommandManager = pAutonomousCommandManager;
        this.mTeleopCommandManager = pTeleopCommandManager;
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

    public DriverInput(Drive pDrivetrain, Elevator pElevator, Intake pIntake, HatchFlower pHatchFlower, CargoSpit pCargoSpit, CommandManager pTeleopCommandManager, CommandManager pAutonomousCommandManager, Data pData) {
        this(pDrivetrain, pElevator, pHatchFlower, pAutonomousCommandManager, pTeleopCommandManager, pCargoSpit, pIntake, pData,false);
//        this.mArm = pArm;
    }

    @Override
    public void modeInit(double pNow) {

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
        if(isDriverAllowingTeleopCommands()) {
            mLog.warn("Requesting command start");
            updateVisionCommands();
        /*
        If the driver started the commands that the superstructure is running and then released the button,
        stop running commands.
        */
        } else if(mTeleopCommandManager.isRunningCommands() && !isDriverAllowingTeleopCommands()) {
            mLog.warn("Requesting command stop: driver no longer allowing commands");
            mTeleopCommandManager.stopRunningCommands();
        }

        if(mAutonomousCommandManager.isRunningCommands() && isAutoOverridePressed()) {
            mLog.warn("Requesting command stop: override pressed");
            mAutonomousCommandManager.stopRunningCommands();
        }

        // Teleop control
        if (!mAutonomousCommandManager.isRunningCommands()) {
            updateDriveTrain();

            if(mOperatorInputCodex.isSet(DriveTeamInputMap.OPERATOR_CARGO_SELECT)) {
                mIsCargo = true;
            } else if(mOperatorInputCodex.isSet(DriveTeamInputMap.OPERATOR_HATCH_SELECT)) {
                mIsCargo = false;
            }

            updateHatchGrabber();
            updateElevator();
            updateArm();
            updateIntake();
        } 


    }

    private void updateIntake() {

        if(mOperatorInputCodex.get(DriveTeamInputMap.OPERATOR_HATCH_FLOWER_CAPTURE_BTN) > 0.5) {
            if(mIsCargo) {
                /*
                Tell both the intake and the cargo spit to start intaking.
                We expect the cargo spit to stop automatically.
                 */
                mIntake.setIntakeState( EIntakeState.GROUND_CARGO ); //TODO may be wrong..?
                mCargoSpit.setIntaking();
            } else {
                /*
                Reset the hatch grabber so it's ready to receive another hatch and tell the intake to start intaking.
                We intake to stop automatically, or when we release the intake button.
                 */
                mHatchFlower.pushHatch();
                mIntake.setIntakeState( EIntakeState.GROUND_HATCH );
            }
        } else if(mOperatorInputCodex.get(DriveTeamInputMap.OPERATOR_SCORE) > 0.5) {
            // If the intake is handing off or stowed, disable these controls
            if(mIntake.isAtPosition( Intake.EWristState.STOWED) || mIntake.isAtPosition(Intake.EWristState.HANDOFF)) {
                if(mIsCargo) {
                    mCargoSpit.setOuttaking();

                } else {
                    mHatchFlower.pushHatch();
                }
            } else {
                // If the intake is on the ground, outtake with the intake instead of scoring mechanisms
//                mIntake.setOuttaking();
                mIntake.setIntakeState( EIntakeState.HANDOFF ); //TODO this probably isn't right
            }
        } else {
            // If the intake button is released, stop everything.
            mCargoSpit.stop();
//            mIntake.stop();
            mIntake.stopIntake();
        }

        if(mOperatorInputCodex.isSet(DriveTeamInputMap.OPERATOR_ARM_MOTION) /* || mIntake.hasHatch() */) { //TODO Subject to change
            mTeleopCommandManager.startCommands(new HandoffHatch(mElevator, mIntake, mHatchFlower));
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

        if(mOperatorInputCodex.isSet(DriveTeamInputMap.OPERATOR_GROUND_POSITION_ELEVATOR)) {
            desiredPosition = EElevatorPosition.GROUND;
        } else if(mOperatorInputCodex.isSet(DriveTeamInputMap.OPERATOR_BOTTOM_POSITION_ELEVATOR)) {
            desiredPosition = mIsCargo ? EElevatorPosition.CARGO_BOTTOM : EElevatorPosition.HATCH_BOTTOM;
        } else if(mOperatorInputCodex.isSet(DriveTeamInputMap.OPERATOR_MIDDLE_POSITION_ELEVATOR)) {
            desiredPosition = mIsCargo ? EElevatorPosition.CARGO_MIDDLE : EElevatorPosition.HATCH_MIDDLE;
        } else if(mOperatorInputCodex.isSet(DriveTeamInputMap.OPERATOR_TOP_POSITION_ELEVATOR)) {
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
//        if (mData.operatorinput.isSet(DriveTeamInputMap.OPERATOR_BOTTOM_POSITION_ELEVATOR)) {
//            mElevator.setDesiredPosition(EElevatorPosition.HATCH_BOTTOM);
//        } else if (mData.operatorinput.isSet(DriveTeamInputMap.OPERATOR_MIDDLE_POSITION_ELEVATOR)) {
//            mElevator.setDesiredPosition(EElevatorPosition.HATCH_MIDDLE);
//        } else if (mData.operatorinput.isSet(DriveTeamInputMap.OPERATOR_TOP_POSITION_ELEVATOR)) {
//            mElevator.setDesiredPosition(EElevatorPosition.TOP);
//        } else if (mData.driverinput.isSet(DriveTeamInputMap.OPERATOR_CONTROL_ELEVATOR)) {
//            double power = mData.operatorinput.get(DriveTeamInputMap.OPERATOR_CONTROL_ELEVATOR);
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
     * Commands the superstructure to update where the arm should move 
     * depending on joystick movements. (in progress)
     */
    protected void updateArm()
    {
        double mult = 1.0;
        //temporarily assuming this setpoint will be set by the operator Y button
        if( mOperatorInputCodex.isSet( DriveTeamInputMap.OPERATOR_ARM_SETPOINT_UP ) )
        {
            mArm.setArmAngle(SystemSettings.ArmPosition.FULLY_UP.getAngle());
        }
        //temporarily assuming this setpoint will be set by the operator A button
        else if( mOperatorInputCodex.isSet( DriveTeamInputMap.OPERATOR_ARM_SETPOINT_DOWN ) )
        {
            mArm.setArmAngle(SystemSettings.ArmPosition.FULLY_DOWN.getAngle());
        }
        //temporarily assuming this setpoint will be set by the operator B button
        else if( mOperatorInputCodex.isSet( DriveTeamInputMap.OPERATOR_ARM_SETPOINT_OUT ) )
        {
            mArm.setArmAngle(SystemSettings.ArmPosition.FULLY_OUT.getAngle());
        }
        //temporarily assuming the arm will be controlled by the operator joystick
        else if( mOperatorInputCodex.isSet( DriveTeamInputMap.OPERATOR_ARM_MOTION ) )
        {
            //mArm.setArmAngle( mArm.getCurrentArmAngle() + mOperatorInputCodex.get( DriveTeamInputMap.OPERATOR_ARM_MOTION ) );
            // System.out.println(mOperatorInputCodex.get( DriveTeamInputMap.OPERATOR_ARM_MOTION ));

            // // Drive the arm directly with the joystick.  Joystick output is -1 to 1
            // // Talon desired output range is -1 to 1
            // // Scale the output by the button pressed
            // // which of these is correct???  both?
            // mArm.setDesiredOutput( mOperatorInputCodex.get( DriveTeamInputMap.OPERATOR_ARM_MOTION ) * mult );
            // mArm.setDesiredOutput( mData.operatorinput.get( DriveTeamInputMap.OPERATOR_ARM_MOTION ) * mult );

            // System.out.println( "+++++++++++++++DriverInput operator joystick: " + mData.operatorinput.get( DriveTeamInputMap.OPERATOR_ARM_MOTION ));

            // Drive the arm to track the joystick
            // Assuming a mapping of 0 to 135 deg for the joysticks -1 to 1
            // angle = ((joystick + 1)/2) * 135
            // double angle = (mData.operatorinput.get( DriveTeamInputMap.OPERATOR_ARM_MOTION ) + 1 ) / 2 * 135;
            
            double angle = this.armJoyStickToAngleScaler.scaleAtoB(mData.operatorinput.get( DriveTeamInputMap.OPERATOR_ARM_MOTION ));
            mArm.setArmAngle(angle);

        }
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
            mTeleopCommandManager.stopRunningCommands();
            mTeleopCommandManager.startCommands(new Delay(30)); // Placeholder
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
