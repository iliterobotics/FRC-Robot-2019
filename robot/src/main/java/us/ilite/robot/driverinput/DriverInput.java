package us.ilite.robot.driverinput;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.flybotix.hfr.codex.Codex;
import com.team254.lib.util.Util;
import edu.wpi.first.wpilibj.Joystick;
import us.ilite.common.config.DriveTeamInputMap;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.types.ETrackingType;
import us.ilite.common.types.input.EInputScale;
import us.ilite.common.types.input.ELogitech310;
import us.ilite.robot.Data;
import us.ilite.robot.commands.ICommand;
import us.ilite.robot.modules.Drive;
import us.ilite.robot.modules.DriveMessage;
import us.ilite.robot.modules.Module;

import java.util.LinkedList;
import java.util.Queue;

public class DriverInput extends Module {

    private static final double DRIVER_SUB_WARP_AXIS_THRESHOLD = 0.5;

    protected final Drive driveTrain;

    private Queue<ICommand> desiredCommandQueue;
    private boolean lastRunCommandQueue;
    private boolean runCommandQueue;
    private Joystick mDriverJoystick;
    private Joystick mOperatorJoystick;

    private Codex<Double, ELogitech310> mDriverInputCodex, mOperatorInputCodex;

    private Data mData;

    public DriverInput(Drive pDrivetrain, Data pData) {
        this.driveTrain = pDrivetrain;
        this.mData = pData;
        this.desiredCommandQueue = new LinkedList<>();
        // this.mDriverInputCodex = mData.driverinput;
        // this.mOperatorInputCodex = mData.operatorinput;
        this.mDriverJoystick = new Joystick(0);
        this.mOperatorJoystick = new Joystick(1);
        this.mDriverInputCodex = mData.driverinput;
        this.mOperatorInputCodex = mData.operatorinput;
    }

    @Override
    public void modeInit(double pNow) {
// TODO Auto-generated method stub

        runCommandQueue = lastRunCommandQueue = false;

    }

    @Override
    public void periodicInput(double pNow) {
        ELogitech310.map(mData.driverinput, mDriverJoystick);
        ELogitech310.map(mData.operatorinput, mOperatorJoystick);
    }

    @Override
    public void update(double pNow) {
//		if(mData.driverinput.get(DriveTeamInputMap.DRIVE_SNAIL_MODE) > 0.5)
//		  scaleInputs = true;
//		else
//		  scaleInputs = false;
        if (!runCommandQueue) {
            updateDriveTrain();
        }
        updateCommands();

    }

    private void updateCommands() {

        runCommandQueue = false;
        for(ELogitech310 l : SystemSettings.kTeleopCommandTriggers) {
            if(mDriverInputCodex.isSet(l)) {
                runCommandQueue = true;
            }
        }

        if (shouldInitializeCommandQueue()) {
            desiredCommandQueue.clear();
            ETrackingType trackingType = null;
            // Switch the limelight to a pipeline and track
            if(mDriverInputCodex.isSet(DriveTeamInputMap.DRIVER_TRACK_TARGET_BTN)) {
                trackingType = ETrackingType.TARGET_LEFT;
            } else if(mDriverInputCodex.isSet(DriveTeamInputMap.DRIVER_TRACK_CARGO_BTN)) {
                trackingType = ETrackingType.CARGO_LEFT;
            } else if(mDriverInputCodex.isSet(DriveTeamInputMap.DRIVER_TRACK_HATCH_BTN)) {
                trackingType = ETrackingType.HATCH_LEFT;
            }

            double pipelineNum;
            if(trackingType != null) {
                if(mDriverInputCodex.isSet(DriveTeamInputMap.DRIVER_NUDGE_SEEK_LEFT)) {
                    pipelineNum = trackingType.getPipeline();
                } else if(mDriverInputCodex.isSet(DriveTeamInputMap.DRIVER_NUDGE_SEEK_RIGHT)) {
                    pipelineNum = trackingType.getPipeline();
                }
            }

            // Set limelight pipeline
            // Add command to the queue
        }
        lastRunCommandQueue = runCommandQueue;
    }

    private void updateDriveTrain() {
        double desiredLeftOutput, desiredRightOutput;

        double rotate = mData.driverinput.get(DriveTeamInputMap.DRIVER_TURN_AXIS);
        rotate = EInputScale.EXPONENTIAL.map(rotate, 2);
        double throttle = -mData.driverinput.get(DriveTeamInputMap.DRIVER_THROTTLE_AXIS);
//		throttle = EInputScale.EXPONENTIAL.map(throttle, 2);

//		if(mElevatorModule.decelerateHeight())
//		{
        // throttle = Utils.clamp(throttle, 0.5);
//		}
        if (mData.driverinput.get(DriveTeamInputMap.DRIVER_SUB_WARP_AXIS) > DRIVER_SUB_WARP_AXIS_THRESHOLD) {
            throttle *= SystemSettings.SNAIL_MODE_THROTTLE_LIMITER;
            rotate *= SystemSettings.SNAIL_MODE_ROTATE_LIMITER;
        }

        rotate = Util.limit(rotate, 0.7);
//		System.out.println("ENGINE THROTTLE " + throttle);
        desiredLeftOutput = throttle + rotate;
        desiredRightOutput = throttle - rotate;

        int leftScalar = desiredLeftOutput < 0 ? -1 : 1;
        int rightScalar = desiredRightOutput < 0 ? -1 : 1;
        desiredLeftOutput = leftScalar * Math.min(Math.abs(desiredLeftOutput), 1);
        desiredRightOutput = rightScalar * Math.min(Math.abs(desiredRightOutput), 1);

//		if(Math.abs(desiredRightOutput) > 0.01 || Math.abs(desiredLeftOutput) > 0.01) {
//			System.out.println("LEFT: " + desiredLeftOutput +"\tRIGHT: " +  desiredRightOutput + "");
//		}

        driveTrain.setDriveMessage(new DriveMessage(desiredLeftOutput, desiredRightOutput, ControlMode.PercentOutput).setNeutralMode(NeutralMode.Brake));

    }

    @Override
    public void shutdown(double pNow) {
// TODO Auto-generated method stub

    }

    /**
     * If we weren't running commands last cycle, initialize.
     */
    public boolean shouldInitializeCommandQueue() {
        return lastRunCommandQueue == false && runCommandQueue == true;
    }

    public boolean canRunCommandQueue() {
        return runCommandQueue;
    }

    public Queue<ICommand> getDesiredCommandQueue() {
        return desiredCommandQueue;
    }


}
