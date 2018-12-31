package us.ilite.robot.modules;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.DemandType;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import control.DriveController;
import control.DriveMotionPlanner;
import control.DriveOutput;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.Timer;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.lib.geometry.Pose2d;
import us.ilite.common.lib.geometry.Pose2dWithCurvature;
import us.ilite.common.lib.geometry.Rotation2d;
import us.ilite.common.lib.trajectory.Trajectory;
import us.ilite.common.lib.trajectory.timing.TimedState;
import us.ilite.common.lib.util.Conversions;
import us.ilite.common.lib.util.ReflectingCSVWriter;
import us.ilite.common.types.drive.EDriveData;
import us.ilite.common.types.sensor.EGyro;
import us.ilite.lib.drivers.Clock;
import us.ilite.lib.drivers.TalonSRXChecker;
import us.ilite.lib.util.SimpleNetworkTable;
import us.ilite.robot.Data;
import us.ilite.robot.hardware.DriveHardware;
import us.ilite.robot.hardware.IDriveHardware;
import us.ilite.robot.loops.Loop;

import java.util.ArrayList;

/**
 * Class for running all drive train control operations from both autonomous and
 * driver-control
 */
public class Drive extends Loop {
	private final ILog mLogger = Logger.createLog(Drive.class);

	private Data mData;

	private DriveHardware mDriveHardware;

	private EDriveState mDriveState;
	private DriveMessage mDriveMessage;

	private DriveController mDriveController;
	private Clock mClock;

	ReflectingCSVWriter<DebugOutput> mDebugLogger;
	ReflectingCSVWriter<DriveMotionPlanner> mMotionPlanLogger;
	DebugOutput debugOutput;

	public Drive(Data data, DriveController pDriveController, Clock pClock)
	{
		this.mData = data;
		this.mDriveController = pDriveController;
		this.mClock = pClock;
		this.mDriveHardware = new DriveHardware();

		mDebugLogger = new ReflectingCSVWriter<>("/home/lvuser/debug.csv", DebugOutput.class);
		debugOutput = new DebugOutput();
	}

	@Override
	public void modeInit(double pNow) {
	    mLogger.info("Starting Drive initialization...");
		Timer initTimer = new Timer();
		initTimer.reset();
		initTimer.start();
		mDriveHardware.init();
	  	setDriveMessage(DriveMessage.kNeutral);
	  	setDriveState(EDriveState.NORMAL);
	  	mLogger.info("Drive initialization took: ", initTimer.get(), " seconds.");
	}

	@Override
	public void periodicInput(double pNow) {
		mData.drive.set(EDriveData.LEFT_POS_INCHES, mDriveHardware.getLeftInches());
		mData.drive.set(EDriveData.RIGHT_POS_INCHES, mDriveHardware.getRightInches());
		mData.drive.set(EDriveData.LEFT_VEL_IPS, mDriveHardware.getLeftVelInches());
		mData.drive.set(EDriveData.RIGHT_VEL_IPS, mDriveHardware.getRightVelInches());
//        mData.drive.set(EDriveData.LEFT_POS_INCHES,0d);
//        mData.drive.set(EDriveData.RIGHT_POS_INCHES, 0d);
//        mData.drive.set(EDriveData.LEFT_VEL_IPS, 0d);
//        mData.drive.set(EDriveData.RIGHT_VEL_IPS,0d);
//		mData.drive.set(EDriveData.LEFT_CURRENT, mDriveHardware.getLeftCurrent());
//		mData.drive.set(EDriveData.RIGHT_CURRENT, mDriveHardware.getRightCurrent());
//		mData.drive.set(EDriveData.LEFT_VOLTAGE, mDriveHardware.getLeftVoltage());
//		mData.drive.set(EDriveData.RIGHT_VOLTAGE, mDriveHardware.getRightVoltage());
 		mData.drive.set(EDriveData.LEFT_VOLTAGE, 0.0);
		mData.drive.set(EDriveData.RIGHT_VOLTAGE, 0.0);

//		mData.drive.set(EDriveData.LEFT_MESSAGE_OUTPUT, mDriveMessage.leftOutput);
//		mData.drive.set(EDriveData.RIGHT_MESSAGE_OUTPUT, mDriveMessage.rightOutput);
//		mData.drive.set(EDriveData.LEFT_MESSAGE_CONTROL_MODE, (double)mDriveMessage.leftControlMode.value);
//		mData.drive.set(EDriveData.RIGHT_MESSAGE_CONTROL_MODE, (double)mDriveMessage.rightControlMode.value);
//		mData.drive.set(EDriveData.LEFT_MESSAGE_NEUTRAL_MODE, (double)mDriveMessage.leftNeutralMode.value);
//		mData.drive.set(EDriveData.RIGHT_MESSAGE_NEUTRAL_MODE, (double)mDriveMessage.rightNeutralMode.value);
//		mData.drive.set(EDriveData.LEFT_MESSAGE_DEMAND_TYPE, (double)mDriveMessage.leftDemandType.value);
//		mData.drive.set(EDriveData.RIGHT_MESSAGE_DEMAND_TYPE, (double)mDriveMessage.rightDemandType.value);
//		mData.drive.set(EDriveData.LEFT_MESSAGE_DEMAND, mDriveMessage.leftDemand);
//		mData.drive.set(EDriveData.RIGHT_MESSAGE_DEMAND, mDriveMessage.rightDemand);

		mData.imu.set(EGyro.YAW_DEGREES, mDriveHardware.getHeading().getDegrees());
//        mData.imu.set(EGyro.YAW_DEGREES, 0d);


//		SimpleNetworkTable.writeCodexToSmartDashboard(EDriveData.class, mData.drive, mClock.getCurrentTime());
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
		mDebugLogger.flush();
//		mMotionPlanLogger.flush();
		mDriveHardware.zero();
	}

	@Override
	public void loop(double pNow) {
		switch(mDriveState) {
			case PATH_FOLLOWING:
				DriveOutput output;
				output = mDriveController.update(
						pNow,
						mData.drive.get(EDriveData.LEFT_POS_INCHES),
						mData.drive.get(EDriveData.RIGHT_POS_INCHES),
						Rotation2d.fromDegrees(mData.imu.get(EGyro.YAW_DEGREES)));

				DriveMessage driveMessage = new DriveMessage(
						Conversions.radiansPerSecondToTicksPer100ms(output.left_velocity),
						Conversions.radiansPerSecondToTicksPer100ms(output.right_velocity),
						ControlMode.Velocity);

				double leftFeedForward = output.left_feedforward_voltage / 12.0;
				double rightFeedforward = output.right_feedforward_voltage / 12.0;

				double leftAccel = Conversions.radiansPerSecondToTicksPer100ms(output.left_accel) / 1000.0;
				double rightAccel = Conversions.radiansPerSecondToTicksPer100ms(output.right_accel) / 1000.0;

				double leftDemand = leftFeedForward + SystemSettings.kDriveVelocity_kD * leftAccel / 1023.0;
				double rightDemand = rightFeedforward + SystemSettings.kDriveVelocity_kD * rightAccel / 1023.0;

				driveMessage.setDemand(DemandType.ArbitraryFeedForward, leftDemand, rightDemand);
				driveMessage.setNeutralMode(NeutralMode.Brake);

				mDriveMessage = driveMessage;

				// Big overhead on logToCsv()!
				debugOutput.logToCsv(pNow, output);
				mDebugLogger.add(debugOutput);
				break;
		}
		mDriveHardware.set(mDriveMessage);
	}

	public void followPath(Trajectory<TimedState<Pose2dWithCurvature>> pPath, boolean pResetPoseToStart) {
		if(mDriveState != EDriveState.PATH_FOLLOWING) {
			mDriveState = EDriveState.PATH_FOLLOWING;
			mDriveHardware.set(new DriveMessage(0.0, 0.0, ControlMode.Velocity)); // Force config
		}

		mDriveController.setTrajectory(pPath, pResetPoseToStart);
	}

	@Override
	public boolean checkModule(double pNow) {
        return mDriveHardware.checkHardware();
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

	private void setDriveState(EDriveState pDriveState) {
		this.mDriveState = pDriveState;
	}

	public synchronized IDriveHardware getDriveHardware() {
	    return mDriveHardware;
    }

    public Drive simulated() {
//		this.mDriveHardware = new SimDriveHardware(mDriveController, mClock);
		return this;
	}

	public class DebugOutput {
		public double t;
		public double targetLeftVel, targetRightVel;
		public double leftVel, rightVel;
		public double targetX, targetY;
		public double x, y;

		public double leftAppliedVolts, rightAppliedVolts;

		public double heading;

		public Pose2d error;

		public void logToCsv(double time, DriveOutput output) {
			t = time;

//			targetLeftVel = Conversions.rotationsToInches(output.left_velocity / (Math.PI * 2.0));
//			targetRightVel = Conversions.rotationsToInches(output.right_velocity / (Math.PI * 2.0));
			targetLeftVel = output.left_velocity;
			targetRightVel = output.right_velocity;
//			leftVel = mData.drive.get(EDriveData.LEFT_VEL_IPS);
//			rightVel = mData.drive.get(EDriveData.RIGHT_VEL_IPS);
			leftVel = mData.drive.get(EDriveData.LEFT_VEL_IPS) / SystemSettings.kDriveWheelCircumference * Math.PI * 2.0;
			rightVel = mData.drive.get(EDriveData.RIGHT_VEL_IPS) / SystemSettings.kDriveWheelCircumference * Math.PI * 2.0;

			targetX = mDriveController.getDriveMotionPlanner().mSetpoint.state().getPose().translation_.x();
			targetY = mDriveController.getDriveMotionPlanner().mSetpoint.state().getPose().translation_.y();
			x = mDriveController.getRobotStateEstimator().getRobotState().getLatestFieldToVehiclePose().translation_.x();
			y = mDriveController.getRobotStateEstimator().getRobotState().getLatestFieldToVehiclePose().translation_.y();

			leftAppliedVolts = mData.drive.get(EDriveData.LEFT_VOLTAGE);
			rightAppliedVolts = mData.drive.get(EDriveData.RIGHT_VOLTAGE);

			heading = mData.imu.get(EGyro.YAW_DEGREES);

			error = mDriveController.getDriveMotionPlanner().error();
		}

		public void logToLiveDashboard() {
		    final NetworkTable livedashboard = NetworkTableInstance.getDefault().getTable("Live Dashboard");
		    final Pose2d robotPose = mDriveController.getRobotStateEstimator().getRobotState().getLatestFieldToVehiclePose();
		    final Pose2d targetPose = mDriveController.getDriveMotionPlanner().mSetpoint.state().getPose();

		    livedashboard.getEntry("Robot X").setDouble(robotPose.translation_.x());
		    livedashboard.getEntry("Robot Y").setDouble(robotPose.translation_.y());
		    livedashboard.getEntry("Robot Heading").setDouble(robotPose.rotation_.getDegrees());

		    livedashboard.getEntry("Path X").setDouble(robotPose.translation_.x());
		    livedashboard.getEntry("Path Y").setDouble(robotPose.translation_.y());
        }
	}

}
	
	



	



  

	