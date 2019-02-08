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
import us.ilite.robot.commands.Delay;
import us.ilite.robot.modules.Arm;
import us.ilite.robot.modules.Drive;
import us.ilite.robot.modules.DriveMessage;
import us.ilite.robot.modules.Module;
import us.ilite.robot.modules.Superstructure;

public class DriverInput extends Module {

    private static final double DRIVER_SUB_WARP_AXIS_THRESHOLD = 0.5;
    private ILog mLog = Logger.createLog(DriverInput.class);

    protected final Drive driveTrain;
    protected final Superstructure mSuperstructure;

    private boolean mDriverStartedCommands;

    private Joystick mDriverJoystick;
    private Joystick mOperatorJoystick;

    private Codex<Double, ELogitech310> mDriverInputCodex, mOperatorInputCodex;

    private Data mData;

    private Arm mArm;

    public DriverInput(Drive pDrivetrain, Superstructure pSuperstructure, Data pData, boolean pSimulated) {
        this.driveTrain = pDrivetrain;
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

    public DriverInput(Drive pDrivetrain, Superstructure pSuperstructure, Data pData, Arm pArm) {
        this(pDrivetrain, pSuperstructure, pData, false);
        this.mArm = pArm;
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
            updateArm();
        } 


    }

    private void updateDriveTrain() {
        if(mData.driverinput.isSet(DriveTeamInputMap.DRIVER_THROTTLE_AXIS) &&
           mData.driverinput.isSet(DriveTeamInputMap.DRIVER_TURN_AXIS) &&
           mData.driverinput.isSet(DriveTeamInputMap.DRIVER_SUB_WARP_AXIS)) {
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
            mult = 0.05;
        }
        //temporarily assuming this setpoint will be set by the operator A button
        if( mOperatorInputCodex.isSet( DriveTeamInputMap.OPERATOR_ARM_SETPOINT_DOWN ) )
        {
            mult = 0.15;
        }
        //temporarily assuming this setpoint will be set by the operator B button
        if( mOperatorInputCodex.isSet( DriveTeamInputMap.OPERATOR_ARM_SETPOINT_OUT ) )
        {
            mult = 0.25;
        }
        //temporarily assuming the arm will be controlled by the operator joystick
        if( mOperatorInputCodex.isSet( DriveTeamInputMap.OPERATOR_ARM_MOTION ) )
        {
            //mArm.setArmAngle( mArm.getCurrentArmAngle() + mOperatorInputCodex.get( DriveTeamInputMap.OPERATOR_ARM_MOTION ) );
            // System.out.println(mOperatorInputCodex.get( DriveTeamInputMap.OPERATOR_ARM_MOTION ));

            // // Drive the arm directly with the joystick.  Joystick output is -1 to 1
            // // Talon desired output range is -1 to 1
            // // Scale the output by the button pressed
            // System.out.println( "DriverInput operator joystick: " + mData.operatorinput.get( DriveTeamInputMap.OPERATOR_ARM_MOTION ));
            // // which of these is correct???  both?
            // mArm.setDesiredOutput( mOperatorInputCodex.get( DriveTeamInputMap.OPERATOR_ARM_MOTION ) * mult );
            // mArm.setDesiredOutput( mData.operatorinput.get( DriveTeamInputMap.OPERATOR_ARM_MOTION ) * mult );

            // Drive the arm to track the joystick
            // Assuming a mapping of 0 to 135 deg for the joysticks -1 to 1
            // angle = ((joystick + 1)/2) * 135
            double angle = (mData.operatorinput.get( DriveTeamInputMap.OPERATOR_ARM_MOTION ) + 1 ) / 2 * 135;
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
