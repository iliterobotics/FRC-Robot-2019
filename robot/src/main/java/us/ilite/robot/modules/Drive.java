package us.ilite.robot.modules;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.DemandType;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import control.DriveController;
import control.DriveOutput;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.lib.geometry.Pose2dWithCurvature;
import us.ilite.common.lib.geometry.Rotation2d;
import us.ilite.common.lib.trajectory.Trajectory;
import us.ilite.common.lib.trajectory.timing.TimedState;
import us.ilite.common.types.drive.EDriveData;
import us.ilite.common.types.sensor.EGyro;
import us.ilite.lib.drivers.Clock;
import us.ilite.robot.Data;
import us.ilite.robot.hardware.DriveHardware;
import us.ilite.robot.hardware.IDriveHardware;
import us.ilite.robot.hardware.SimDriveHardware;
import us.ilite.robot.loops.Loop;

/**
 * Class for running all drive train control operations from both autonomous and
 * driver-control
 */
public class Drive extends Loop {
	private final ILog mLogger = Logger.createLog(Drive.class);

	private Data mData;

	private IDriveHardware mDriveHardware;

	private EDriveState mDriveState;
	private DriveMessage mDriveMessage;

	private DriveController mDriveController;
	private Clock mClock;

	public Drive(Data data, DriveController pDriveController, Clock pClock)
	{
		this.mData = data;
		this.mDriveController = pDriveController;
		this.mClock = pClock;
		this.mDriveHardware = new DriveHardware();
	}

	@Override
	public void modeInit(double pNow) {
	  	setDriveMessage(DriveMessage.kNeutral);
	  	setDriveState(EDriveState.NORMAL);
	}

	@Override
	public void periodicInput(double pNow) {
		mData.drive.set(EDriveData.LEFT_POS_INCHES, mDriveHardware.getLeftInches());
		mData.drive.set(EDriveData.RIGHT_POS_INCHES, mDriveHardware.getRightInches());
		mData.drive.set(EDriveData.LEFT_VEL_IPS, mDriveHardware.getLeftVelInches());
		mData.drive.set(EDriveData.RIGHT_VEL_IPS, mDriveHardware.getRightInches());
		mData.drive.set(EDriveData.LEFT_CURRENT, mDriveHardware.getLeftCurrent());
		mData.drive.set(EDriveData.RIGHT_CURRENT, mDriveHardware.getRightCurrent());
		mData.drive.set(EDriveData.LEFT_VOLTAGE, mDriveHardware.getLeftVoltage());
		mData.drive.set(EDriveData.RIGHT_VOLTAGE, mDriveHardware.getRightVoltage());
		
		mData.drive.set(EDriveData.LEFT_MESSAGE_OUTPUT, mDriveMessage.leftOutput);
		mData.drive.set(EDriveData.RIGHT_MESSAGE_OUTPUT, mDriveMessage.rightOutput);
		mData.drive.set(EDriveData.LEFT_MESSAGE_CONTROL_MODE, (double)mDriveMessage.leftControlMode.value);
		mData.drive.set(EDriveData.RIGHT_MESSAGE_CONTROL_MODE, (double)mDriveMessage.rightControlMode.value);
		mData.drive.set(EDriveData.LEFT_MESSAGE_NEUTRAL_MODE, (double)mDriveMessage.leftNeutralMode.value);
		mData.drive.set(EDriveData.RIGHT_MESSAGE_NEUTRAL_MODE, (double)mDriveMessage.rightNeutralMode.value);
		mData.drive.set(EDriveData.LEFT_MESSAGE_DEMAND_TYPE, (double)mDriveMessage.leftDemandType.value);
		mData.drive.set(EDriveData.RIGHT_MESSAGE_DEMAND_TYPE, (double)mDriveMessage.rightDemandType.value);
		mData.drive.set(EDriveData.LEFT_MESSAGE_DEMAND, mDriveMessage.leftDemand);
		mData.drive.set(EDriveData.RIGHT_MESSAGE_DEMAND, mDriveMessage.rightDemand);

		mData.imu.set(EGyro.YAW_DEGREES, mDriveHardware.getHeading().getDegrees());
	}

	@Override
	public void update(double pNow) {
		if(mDriveState != EDriveState.NORMAL) {
			mLogger.error("Invalid drive state - maybe you meant to run this a high frequency?");
		} else {
			mDriveHardware.set(mDriveMessage);
		}
	}
	
	@Override
	public void shutdown(double pNow) {
		mDriveHardware.zero();
	}

	@Override
	public void loop(double pNow) {
		switch(mDriveState) {
			case PATH_FOLLOWING:
				if(mDriveController.isDone()) {
					setDriveMessage(DriveMessage.kNeutral);
				} else {
					DriveOutput output = mDriveController.getOutput(
							pNow,
							mData.drive.get(EDriveData.LEFT_POS_INCHES),
							mData.drive.get(EDriveData.RIGHT_POS_INCHES),
							Rotation2d.fromDegrees(mData.imu.get(EGyro.YAW_DEGREES)));
					DriveMessage driveMessage = new DriveMessage(output.left_velocity, output.right_velocity, ControlMode.Velocity);

					double leftFeedForward = output.left_feedforward_voltage / 12.0;
					double rightFeedforward = output.right_feedforward_voltage / 12.0;

					double leftAccel = radiansPerSecondToTicksPer100ms(output.left_accel) / 1000.0;
					double rightAccel = radiansPerSecondToTicksPer100ms(output.right_accel) / 1000.0;

					double leftDemand = leftFeedForward + SystemSettings.kDriveVelocity_kD * leftAccel / 1023.0;
					double rightDemand = rightFeedforward + SystemSettings.kDriveVelocity_kD * rightAccel / 1023.0;

					driveMessage.setDemand(DemandType.ArbitraryFeedForward, leftDemand, rightDemand);

					setDriveMessage(driveMessage);
				}
				break;
			case NORMAL:
			default:
				mDriveHardware.set(mDriveMessage);
				break;
		}
	}

	public void followPath(Trajectory<TimedState<Pose2dWithCurvature>> pPath, boolean pResetPoseToStart) {
		if(mDriveState != EDriveState.PATH_FOLLOWING) {
			mDriveState = EDriveState.PATH_FOLLOWING;
		}

		mDriveController.setTrajectory(pPath, pResetPoseToStart);
	}

	@Override
	public void checkModule(double pNow) {

	}

	private static double rotationsToInches(double rotations) {
		return rotations * (SystemSettings.kDriveWheelDiameterInches * Math.PI);
	}

	private static double rpmToInchesPerSecond(double rpm) {
		return rotationsToInches(rpm) / 60;
	}

	private static double inchesToRotations(double inches) {
		return inches / (SystemSettings.kDriveWheelDiameterInches * Math.PI);
	}

	private static double inchesPerSecondToRpm(double inches_per_second) {
		return inchesToRotations(inches_per_second) * 60;
	}

	private static double radiansPerSecondToTicksPer100ms(double rad_s) {
		return rad_s / (Math.PI * 2.0) * SystemSettings.kDriveTicksPerRotation / 10.0;
	}

	public DriveController getDriveController() {
		return mDriveController;
	}

	public synchronized void zero() {
		mDriveHardware.zero();
	}
	
	public synchronized void setDriveMessage(DriveMessage pDriveMessage) {
		this.mDriveMessage = pDriveMessage;
	}

	public synchronized void setDriveState(EDriveState pDriveState) {
		this.mDriveState = pDriveState;
	}

	public synchronized IDriveHardware getDriveHardware() {
	    return mDriveHardware;
    }

    public Drive simulated() {
		this.mDriveHardware = new SimDriveHardware(mDriveController, mClock);
		return this;
	}

}
	
	



	



  

	