package us.ilite.robot.driverinput;

import java.util.LinkedList;
import java.util.Queue;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.team254.lib.util.Util;

import edu.wpi.first.wpilibj.Joystick;
import us.ilite.common.config.DriveTeamInputMap;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.types.input.EInputScale;
import us.ilite.common.types.input.ELogitech310;
import us.ilite.robot.Data;
import us.ilite.robot.commands.ICommand;
import us.ilite.robot.modules.Drive;
import us.ilite.robot.modules.DriveMessage;
import us.ilite.robot.modules.Module;

/**
 * Handles the translation of driver input to robot action. This class also allows us to
 * run commands in teleop, temporarily taking away driver control. Control of each module
 * is handled in a method called update[insert module name here](), and each is called in
 * the update() method. The updateDrivetrain() method is intended to be overridden by a
 * class extending DriverInput, which means we can switch control schemes (tank drive,
 * arcade drive, split arcade drive, curvature drive, etc.) without changing code controlling
 * other parts of the robot. Any interaction between modules, such as preventing collisions
 * and automating tasks (such as retracting the intakes when the carriage is open and the beam
 * break is broken) is also handled here, NOT in individual modules.
 */
public class DriverInput extends Module {

    protected final Drive driveTrain;
    private boolean scaleInputs;
    private boolean currentDriverToggle, lastDriverToggle, currentOperatorToggle, lastOperatorToggle;

    private Queue<ICommand> desiredCommandQueue;
    private boolean lastCanRunCommandQueue;
    private boolean canRunCommandQueue;
    private Joystick mDriverJoystick;
    private Joystick mOperatorJoystick;


    private Data mData;

    public DriverInput(Drive pDrivetrain, Data pData) {
        this.driveTrain = pDrivetrain;
        this.mData = pData;
        this.desiredCommandQueue = new LinkedList<>();
        this.mDriverJoystick = new Joystick(0);
        this.mOperatorJoystick = new Joystick(1);
        scaleInputs = false;
    }

    @Override
    public void modeInit(double pNow) {

        canRunCommandQueue = lastCanRunCommandQueue = false;

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
        // Only give the driver control if we aren't allowed to run a command.
        if (!canRunCommandQueue) {
            updateDriveTrain();
        }
        updateCommands();

    }

    private void updateCommands() {

//canRunCommandQueue = is a button triggered?

        // If the driver has pressed a button indicating that we should run a command, add that command to the queue
        if (shouldInitializeCommandQueue()) {
            desiredCommandQueue.clear();
//desiredCommandQueue.add(<command>);
        }
        lastCanRunCommandQueue = canRunCommandQueue;
    }


    private void updateDriveTrain() {
        double desiredLeftOutput, desiredRightOutput;

        double rotate = mData.driverinput.get(DriveTeamInputMap.DRIVER_TURN_AXIS);
        rotate = EInputScale.EXPONENTIAL.map(rotate, 2);
        double throttle = -mData.driverinput.get(DriveTeamInputMap.DRIVER_THROTTLE_AXIS);
//		throttle = EInputScale.EXPONENTIAL.map(throttle, 2);

//		if(mElevatorModule.decelerateHeight())
//		{
//		  throttle = Utils.clamp(throttle, 0.5);
//		}
        if (mData.driverinput.get(DriveTeamInputMap.DRIVER_SUB_WARP_AXIS) > 0.5) {
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
     *
     * @return Whether we have changed from "not wanting to run commands" to "wanting to run commands" since the last cycle.
     */
    public boolean shouldInitializeCommandQueue() {
        return lastCanRunCommandQueue == false && canRunCommandQueue == true;
    }

    public boolean canRunCommandQueue() {
        return canRunCommandQueue;
    }

    public Queue<ICommand> getDesiredCommandQueue() {
        return desiredCommandQueue;
    }


}
