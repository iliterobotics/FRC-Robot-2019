package us.ilite.robot;

import com.flybotix.hfr.util.log.ELevel;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import com.revrobotics.CANEncoder;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import us.ilite.common.lib.control.DriveController;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import us.ilite.common.lib.trajectory.TrajectoryGenerator;
import us.ilite.common.types.drive.EDriveData;
import us.ilite.robot.auto.paths.TestAuto;
import us.ilite.common.config.SystemSettings;
import com.team254.lib.geometry.Pose2dWithCurvature;
import com.team254.lib.trajectory.Trajectory;
import com.team254.lib.trajectory.timing.TimedState;
import com.team254.lib.trajectory.timing.TimingConstraint;
import us.ilite.lib.drivers.Clock;
import us.ilite.robot.commands.CharacterizeDrive;
import us.ilite.robot.commands.CommandQueue;
import us.ilite.robot.commands.FollowTrajectory;
import us.ilite.robot.driverinput.DriverInput;
import us.ilite.robot.loops.LoopManager;
import us.ilite.robot.modules.Drive;
import us.ilite.robot.modules.Limelight;
import us.ilite.robot.modules.ModuleList;

import java.util.Arrays;
import java.util.List;

public class Robot extends TimedRobot {
    
    private ILog mLogger = Logger.createLog(this.getClass());

    private CommandQueue mCommandQueue = new CommandQueue();

    // It sure would be convenient if we could reduce this to just a LoopManager...Will have to test timing of Codex first
    private LoopManager mLoopManager = new LoopManager(SystemSettings.kControlLoopPeriod);
    private ModuleList mRunningModules = new ModuleList();

    private Clock mClock = new Clock();
    private Data mData = new Data();
    private Timer initTimer = new Timer();

    // Module declarations here
    private DriveController mDriveController = new DriveController(new StrongholdProfile());
    private Drive mDrive = new Drive(mData, mDriveController);
    private DriverInput mDriverInput = new DriverInput(mDrive, mData);
    private Limelight mLimelight = new Limelight();

    private Trajectory<TimedState<Pose2dWithCurvature>> trajectory;

    private CANSparkMax mTestNeo = new CANSparkMax(0, MotorType.kBrushless);
    private CANEncoder mTestNeoEncoder = mTestNeo.getEncoder();
    private Joystick mTestJoystick = new Joystick(2);

    @Override
    public void robotInit() {
        initTimer.reset();
        initTimer.start();
        Logger.setLevel(ELevel.INFO);
        mLogger.info("Starting Robot Initialization...");

        mRunningModules.setModules();

        TrajectoryGenerator mTrajectoryGenerator = new TrajectoryGenerator(mDriveController);
        List<TimingConstraint<Pose2dWithCurvature>> kTrajectoryConstraints = Arrays.asList(/*new CentripetalAccelerationConstraint(60.0)*/);
        trajectory = mTrajectoryGenerator.generateTrajectory(false, TestAuto.kPath, kTrajectoryConstraints, 60.0, 80.0, 12.0);


        initTimer.stop();
        mLogger.info("Robot initialization finished. Took: ", initTimer.get(), " seconds");
    }

    /**
     * This contains code run in ALL robot modes.
     * It's also important to note that this runs AFTER mode-specific code
     */
    @Override
    public void robotPeriodic() {
//        mLogger.info(this.toString());

        mClock.cycleEnded();
    }

    @Override
    public void autonomousInit() {
        initTimer.reset();
        initTimer.start();
        mLogger.info("Starting Autonomous Initialization...");


        mRunningModules.setModules();
        mRunningModules.modeInit(mClock.getCurrentTime());
        mRunningModules.periodicInput(mClock.getCurrentTime());

        mLoopManager.setRunningLoops(mDrive);
        mLoopManager.start();

        mCommandQueue.setCommands(new FollowTrajectory(trajectory, mDrive, true));
    //    mCommandQueue.setCommands(new CharacterizeDrive(mDrive, false, false));
        mCommandQueue.init(mClock.getCurrentTime());

        initTimer.stop();
        mLogger.info("Autonomous initialization finished. Took: ", initTimer.get(), " seconds");
    }

    @Override
    public void autonomousPeriodic() {
        // mRunningModules.periodicInput(mClock.getCurrentTime());
        mCommandQueue.update(mClock.getCurrentTime());
        // mRunningModules.update(mClock.getCurrentTime());
//        mDrive.flushTelemetry();
    }

    @Override
    public void teleopInit() {
        mRunningModules.setModules(mDriverInput, mLimelight);
        mRunningModules.modeInit(mClock.getCurrentTime());
        mRunningModules.periodicInput(mClock.getCurrentTime());

        mLoopManager.setRunningLoops(mDrive);
        mLoopManager.start();
    }

    @Override
    public void teleopPeriodic() {
        mRunningModules.periodicInput(mClock.getCurrentTime());
        mRunningModules.update(mClock.getCurrentTime());
        Data.kSmartDashboard.putDouble("left_vel", mData.drive.get(EDriveData.LEFT_VEL_IPS));
        Data.kSmartDashboard.putDouble("right_vel", mData.drive.get(EDriveData.RIGHT_VEL_IPS));
        Data.kSmartDashboard.putDouble("Neo Position", mTestNeoEncoder.getPosition());
        Data.kSmartDashboard.putDouble("Neo Velocity", mTestNeoEncoder.getVelocity());
        mTestNeo.set(mTestJoystick.getX());
    }

    @Override
    public void disabledInit() {
        mLogger.info("Disabled Initialization");
        mRunningModules.shutdown(mClock.getCurrentTime());
        mLoopManager.stop();
        mCommandQueue.shutdown(mClock.getCurrentTime());
    }

    @Override
    public void disabledPeriodic() {
    }

    @Override
    public void testInit() {
        mRunningModules.setModules(mDrive);
        mRunningModules.modeInit(mClock.getCurrentTime());
        mRunningModules.periodicInput(mClock.getCurrentTime());
        mRunningModules.checkModule(mClock.getCurrentTime());

        mLoopManager.start();
    }

    @Override
    public void testPeriodic() {

//        mRunningModules.periodicInput(mClock.getCurrentTime());
//        mRunningModules.update(mClock.getCurrentTime());
    }

    public String toString() {

        String mRobotMode = "Unknown";
        String mRobotEnabledDisabled = "Unknown";
        double mNow = Timer.getFPGATimestamp();

        if(this.isAutonomous()) {
            mRobotMode = "Autonomous";
        }
        if(this.isOperatorControl()) {
            mRobotMode = "Operator Control";
        }
        if(this.isTest()) {
            mRobotEnabledDisabled = "Test";
        }

        if(this.isEnabled()) {
            mRobotEnabledDisabled = "Enabled";
        }
        if(this.isDisabled()) {
            mRobotEnabledDisabled = "Disabled";
        }

        return String.format("State: %s\tMode: %s\tTime: %s", mRobotEnabledDisabled, mRobotMode, mNow);

    }

}
