package us.ilite.robot.modules;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.DemandType;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.LogOutput;
import com.flybotix.hfr.util.log.Logger;
import com.team254.lib.geometry.Pose2d;
import com.team254.lib.geometry.Pose2dWithCurvature;
import com.team254.lib.geometry.Rotation2d;
import com.team254.lib.trajectory.Trajectory;
import com.team254.lib.trajectory.timing.TimedState;
import com.team254.lib.util.ReflectingCSVWriter;

import us.ilite.common.Data;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.lib.control.DriveController;
import com.team254.frc2018.planners.DriveMotionPlanner;
import com.team254.lib.physics.DriveOutput;
import us.ilite.common.lib.util.Conversions;
import us.ilite.common.lib.util.PerfTimer;
import us.ilite.common.types.drive.EDriveData;
import us.ilite.common.types.sensor.EGyro;
import us.ilite.lib.drivers.Clock;
import us.ilite.robot.hardware.DriveHardware;
import us.ilite.robot.hardware.IDriveHardware;
import us.ilite.robot.hardware.SimDriveHardware;
import us.ilite.robot.loops.Loop;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class for running all drive train control operations from both autonomous and
 * driver-control.
 * TODO Support for rotation trajectories
 * TODO Turn-to-heading with Motion Magic
 */
public class Drive extends Loop {
	private final ILog mLogger = Logger.createLog(Drive.class);

	private Data mData;

	private IDriveHardware mDriveHardware;
	private Rotation2d mGyroOffset = new Rotation2d();

	private EDriveState mDriveState;
	private DriveMessage mDriveMessage;

	private DriveController mDriveController;
	private Clock mSimClock = null;

	ReflectingCSVWriter<DebugOutput> mDebugLogger = null;
	DebugOutput debugOutput = new DebugOutput();

//	PerfTimer mUpdateTimer = new PerfTimer().alwayLog();
//	PerfTimer mCalculateTimer = new PerfTimer().alwayLog().setLogMessage("Calculate: %s");
//	PerfTimer mMotionPlannerTimer = new PerfTimer().alwayLog().setLogMessage("Planner: %s");

	public Drive(Data data, DriveController pDriveController, Clock pSimClock, boolean pSimulated)
	{
		this.mData = data;
		this.mDriveController = pDriveController;
		if(pSimulated) {
			this.mSimClock = pSimClock;
			this.mDriveHardware = new SimDriveHardware(mSimClock, mDriveController.getRobotProfile());
		} else {
			this.mDriveHardware = new DriveHardware();
		}

		this.mDriveHardware.init();
	}

	public Drive(Data data, DriveController pDriveController) {
		this(data, pDriveController, null, false);
	}

	public void startCsvLogging() {
		mDebugLogger = new ReflectingCSVWriter<>("/home/lvuser/debug.csv", DebugOutput.class);
		debugOutput = new DebugOutput();
		mDebugLogger.add(debugOutput);
	}

	public void stopCsvLogging() {
		if(mDebugLogger != null) {
			mDebugLogger.flush();
			mDebugLogger = null;
		}
	}

	@Override
	public void modeInit(double pNow) {
		mDriveController.setPlannerMode(DriveMotionPlanner.PlannerMode.FEEDBACK);
        // Other gains to try: (2.0, 0.7), (0.65, 0.175), (0.0, 0.0)
		mDriveController.getController().setGains(2.0, 0.7);

		mDriveHardware.zero();

	  	setDriveMessage(DriveMessage.kNeutral);
	  	setDriveState(EDriveState.NORMAL);

	  	startCsvLogging();
	}

	@Override
	public void periodicInput(double pNow) {

		mData.drive.set(EDriveData.LEFT_POS_INCHES, mDriveHardware.getLeftInches());
		mData.drive.set(EDriveData.RIGHT_POS_INCHES, mDriveHardware.getRightInches());
//		mData.drive.set(EDriveData.LEFT_VEL_IPS, mDriveHardware.getLeftVelInches());
//		mData.drive.set(EDriveData.RIGHT_VEL_IPS, mDriveHardware.getRightVelInches());
		mData.drive.set(EDriveData.LEFT_VEL_TICKS, (double)mDriveHardware.getLeftVelTicks());
		mData.drive.set(EDriveData.RIGHT_VEL_TICKS, (double)mDriveHardware.getRightVelTicks());

//		mData.drive.set(EDriveData.LEFT_CURRENT, mDriveHardware.getLeftCurrent());
//		mData.drive.set(EDriveData.RIGHT_CURRENT, mDriveHardware.getRightCurrent());
//		mData.drive.set(EDriveData.LEFT_VOLTAGE, mDriveHardware.getLeftVoltage());
//		mData.drive.set(EDriveData.RIGHT_VOLTAGE, mDriveHardware.getRightVoltage());
// 		mData.drive.set(EDriveData.LEFT_VOLTAGE, 0.0);
//		mData.drive.set(EDriveData.RIGHT_VOLTAGE, 0.0);
//
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
//
		mData.imu.set(EGyro.YAW_DEGREES, getHeading().getDegrees());

//		SimpleNetworkTable.writeCodexToSmartDashboard(EDriveData.class, mData.drive, mClock.getCurrentTime());
	}

	@Override
	public void update(double pNow) {
        if(mDriveState != EDriveState.NORMAL) {
			mLogger.error("Invalid drive state - maybe you meant to run this a high frequency?");
			mDriveState = EDriveState.NORMAL;
		} else {
			mDriveHardware.set(mDriveMessage);
		}
	}
	
	@Override
	public void shutdown(double pNow) {
		stopCsvLogging();
		mDriveHardware.zero();
	}

	@Override
	public void loop(double pNow) {
//		mUpdateTimer.start();
		switch(mDriveState) {
			case PATH_FOLLOWING:
//				mCalculateTimer.start();
//				mMotionPlannerTimer.start();
				// Update controller - calculates new robot position and retrieves motion planner output
				DriveOutput output = mDriveController.update(
						pNow,
						mData.drive.get(EDriveData.LEFT_POS_INCHES),
						mData.drive.get(EDriveData.RIGHT_POS_INCHES),
						Rotation2d.fromDegrees(mData.imu.get(EGyro.YAW_DEGREES)));
//				mMotionPlannerTimer.stop();
				// Convert controller output into something compatible with Talons
				DriveMessage driveMessage = new DriveMessage(
						Conversions.radiansPerSecondToTicksPer100ms(output.left_velocity),
						Conversions.radiansPerSecondToTicksPer100ms(output.right_velocity),
						ControlMode.Velocity);

				double leftAccel = Conversions.radiansPerSecondToTicksPer100ms(output.left_accel) / 1000.0;
				double rightAccel = Conversions.radiansPerSecondToTicksPer100ms(output.right_accel) / 1000.0;

				/*
				 * SP = setpoint, PV = process variable
				 * CTRE only uses -kD * dPV/dt for derivative output, not kD * de/dt.
				 * This is because SP (provided by robot @ x Hz) gets updated less frequently than PV (updated by Talon at 1000Hz),
				 * which means that the derivative of error would be highly inaccurate and cause oscillation.
				 * Since kD * de/dt = kD * (SP - PV)/dt = ((kD * SP) - (kD * PV)) / dt, and dt = 1 for the Talon, we can add the
				 * setpoint derivative calculation back in.
					*/
				double leftDemand = (output.left_feedforward_voltage / 12.0) + SystemSettings.kDriveVelocity_kD * leftAccel / 1023.0;
				double rightDemand = (output.right_feedforward_voltage / 12.0) + SystemSettings.kDriveVelocity_kD * rightAccel / 1023.0;

				// Add in the feedforward we've calculated and set motors to Brake mode
				driveMessage.setDemand(DemandType.ArbitraryFeedForward, leftDemand, rightDemand);
				driveMessage.setNeutralMode(NeutralMode.Brake);

				mDriveMessage = driveMessage;
//				mCalculateTimer.stop();

				if(mDebugLogger != null) {
					debugOutput.update(pNow, mDriveMessage);
					mDebugLogger.add(debugOutput);
				}

				break;
			case NORMAL:
				break;
			default:
				mLogger.warn("Got drive state: " + mDriveState+" which is unhandled");
				break;
		}
		mDriveHardware.set(mDriveMessage);
//		mUpdateTimer.stop();
	}

	public void setPathFollowing() {
		mDriveState = EDriveState.PATH_FOLLOWING;
		mDriveHardware.configureMode(ControlMode.Velocity);
		mDriveHardware.set(new DriveMessage(0.0, 0.0, ControlMode.Velocity));
	}

	public void setNormal() {
		mDriveState = EDriveState.NORMAL;
	}

	public void setPath(Trajectory<TimedState<Pose2dWithCurvature>> pPath, boolean pResetPoseToStart) {
		mDriveController.setTrajectory(pPath, pResetPoseToStart);
		if(pResetPoseToStart) {
			mDriveHardware.zero();
		}
	}

	public void openLoop() {
		if(mDriveState != EDriveState.NORMAL) mDriveState = EDriveState.NORMAL;
	}

	@Override
	public boolean checkModule(double pNow) {
        return mDriveHardware.checkHardware();
	}

	private void setDriveState(EDriveState pDriveState) {
		this.mDriveState = pDriveState;
	}

	public synchronized void zero() {
		mDriveHardware.zero();
	}

	public synchronized void flushTelemetry() {
		if(mDebugLogger != null) {
			mDebugLogger.write();
		}
	}

	public synchronized void setDriveMessage(DriveMessage pDriveMessage) {
		this.mDriveMessage = pDriveMessage;
	}

	public synchronized DriveController getDriveController() {
		return mDriveController;
	}

	public synchronized IDriveHardware getDriveHardware() {
	    return mDriveHardware;
    }

    public synchronized DriveMessage getDriveMessage() {
		return mDriveMessage;
	}

	public synchronized Rotation2d getHeading() {
		return mDriveHardware.getHeading().rotateBy(mGyroOffset);
	}

	public synchronized void setHeading(Rotation2d pHeading) {
		mGyroOffset = pHeading.rotateBy(mDriveHardware.getHeading().inverse());
	}

    public Drive simulated() {
//		this.mDriveHardware = new SimDriveHardware(mDriveController, mClock);
		return this;
	}

	public Clock getSimClock() {
		return mSimClock;
	}

	public class DebugOutput {

		public double t = 0.0;

		public double targetLeftVel = 0.0, targetRightVel = 0.0, leftVel = 0.0, rightVel = 0.0;
		public double targetX = 0.0, targetY = 0.0, x = 0.0, y = 0.0;

//		public double leftAppliedVolts = 0.0, rightAppliedVolts = 0.0;

//		public double heading = 0.0;

//		public Pose2d error = new Pose2d();

		public List<LogOutput> status = new ArrayList<>();

		public void update(double time, DriveMessage output) {
			t = time;

//			targetLeftVel = Conversions.rotationsToInches(output.left_velocity / (Math.PI * 2.0));
//			targetRightVel = Conversions.rotationsToInches(output.right_velocity / (Math.PI * 2.0));
//			targetLeftVel = mDriveHardware.getLeftTarget();
//			targetRightVel = mDriveHardware.getRightTarget();
			targetLeftVel = output.leftOutput;
			targetRightVel = output.rightOutput;


			leftVel = mData.drive.get(EDriveData.LEFT_VEL_TICKS);
			rightVel = mData.drive.get(EDriveData.RIGHT_VEL_TICKS);

//			status = Logger.getRecentLogs().stream().filter(logOutput -> logOutput.thread.equals(this.getClass().getName())).collect(Collectors.toList());
			targetX = mDriveController.getDriveMotionPlanner().mSetpoint.state().getPose().getTranslation().x();
			targetY = mDriveController.getDriveMotionPlanner().mSetpoint.state().getPose().getTranslation().y();
			x = mDriveController.getCurrentPose().getTranslation().x();
			y = mDriveController.getCurrentPose().getTranslation().y();

//			leftAppliedVolts = mData.drive.get(EDriveData.LEFT_VOLTAGE);
//			rightAppliedVolts = mData.drive.get(EDriveData.RIGHT_VOLTAGE);

//			heading = mData.imu.get(EGyro.YAW_DEGREES);

//			error = mDriveController.getDriveMotionPlanner().error();
		}

	}

}
	
	



	



  

	