package us.ilite.robot;

import java.util.Arrays;
import java.util.List;

import com.flybotix.hfr.codex.CodexMetadata;
import com.flybotix.hfr.codex.ICodexTimeProvider;

import com.flybotix.hfr.util.log.ELevel;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import com.revrobotics.CANEncoder;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.team254.lib.geometry.Pose2dWithCurvature;
import com.team254.lib.geometry.Rotation2d;
import com.team254.lib.trajectory.Trajectory;
import com.team254.lib.trajectory.timing.TimedState;
import com.team254.lib.trajectory.timing.TimingConstraint;

import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.io.Network;

import com.team254.lib.geometry.Pose2dWithCurvature;
import com.team254.lib.trajectory.Trajectory;
import com.team254.lib.trajectory.timing.TimedState;
import com.team254.lib.trajectory.timing.TimingConstraint;
import us.ilite.common.lib.trajectory.TrajectoryGenerator;

import us.ilite.lib.drivers.Clock;
import us.ilite.robot.auto.paths.TestAuto;
import us.ilite.robot.commands.CommandQueue;
import us.ilite.robot.commands.TurnToDegree;
import us.ilite.robot.driverinput.DriverInput;
import us.ilite.robot.loops.LoopManager;
import us.ilite.robot.modules.Drive;
import us.ilite.robot.modules.Limelight;
import us.ilite.robot.modules.ModuleList;
import us.ilite.robot.modules.Superstructure;
import us.ilite.common.lib.control.DriveController;

public class Robot extends TimedRobot {
    
    private ILog mLogger = Logger.createLog(this.getClass());

    // It sure would be convenient if we could reduce this to just a LoopManager...Will have to test timing of Codex first
    private LoopManager mLoopManager = new LoopManager(SystemSettings.kControlLoopPeriod);
    private ModuleList mRunningModules = new ModuleList();
    private CommandQueue mCommandQueue = new CommandQueue();

    private Clock mSystemClock = new Clock();
    private Data mData = new Data();
    private Timer initTimer = new Timer();
    private SystemSettings mSettings = new SystemSettings();


    // Module declarations here
    private Superstructure mSuperstructure = new Superstructure();
    private DriveController mDriveController = new DriveController(new StrongholdProfile());
    private Drive mDrive = new Drive(mData, mDriveController);
    
    private DriverInput mDriverInput = new DriverInput(mDrive, mSuperstructure, mData);
    private Limelight mLimelight = new Limelight();

    private Trajectory<TimedState<Pose2dWithCurvature>> trajectory;

    private CANSparkMax mTestNeo = new CANSparkMax(0, MotorType.kBrushless);
    private CANEncoder mTestNeoEncoder = mTestNeo.getEncoder();
    private Joystick mTestJoystick = new Joystick(2);

    @Override
    public void robotInit() {
        // Init static variables and get singleton instances first
        Network.getInstance();

        ICodexTimeProvider provider = new ICodexTimeProvider() {
            public long getTimestamp() {
                return (long)mSystemClock.getCurrentTimeInNanos();
            }
        };
        CodexMetadata.overrideTimeProvider(provider);

        // Init the actual robot
        initTimer.reset();
        initTimer.start();
        Logger.setLevel(ELevel.INFO);
        mLogger.info("Starting Robot Initialization...");

        mSettings.writeToNetworkTables();

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

        mSystemClock.cycleEnded();
    }

    @Override
    public void autonomousInit() {
        
        initTimer.reset();
        initTimer.start();
        mLogger.info("Starting Autonomous Initialization...");

        mSettings.loadFromNetworkTables();

        // Init modules after commands are set
        mRunningModules.setModules(mSuperstructure);
        mRunningModules.modeInit(mSystemClock.getCurrentTime());
        mRunningModules.periodicInput(mSystemClock.getCurrentTime());

        mLoopManager.setRunningLoops(mDrive);
        mLoopManager.start();

        // mCommandQueue.setCommands(new FollowTrajectory(trajectory, mDrive, true));
//        mCommandQueue.setCommands(new CharacterizeDrive(mDrive, false, false));
        mCommandQueue.setCommands(
            new TurnToDegree(mDrive, Rotation2d.fromDegrees(90.0), 2.5, mData), 
            new TurnToDegree(mDrive, Rotation2d.fromDegrees(-90.0), 2.5, mData));
        mCommandQueue.init(mClock.getCurrentTime());

        initTimer.stop();
        mLogger.info("Autonomous initialization finished. Took: ", initTimer.get(), " seconds");
    }

    @Override
    public void autonomousPeriodic() {
        mRunningModules.periodicInput(mSystemClock.getCurrentTime());
        mRunningModules.update(mSystemClock.getCurrentTime());
//        mDrive.flushTelemetry();
    }

    @Override
    public void teleopInit() {
        mRunningModules.setModules(mDriverInput, mLimelight);
        mRunningModules.modeInit(mSystemClock.getCurrentTime());
        mRunningModules.periodicInput(mSystemClock.getCurrentTime());

        mLoopManager.setRunningLoops(mDrive);
        mLoopManager.start();
    }

    @Override
    public void teleopPeriodic() {
        mRunningModules.periodicInput(mSystemClock.getCurrentTime());
        mRunningModules.update(mSystemClock.getCurrentTime());
        Data.kSmartDashboard.putDouble("Neo Position", mTestNeoEncoder.getPosition());
        Data.kSmartDashboard.putDouble("Neo Velocity", mTestNeoEncoder.getVelocity());
        mTestNeo.set(mTestJoystick.getX());
    }

    @Override
    public void disabledInit() {
        mLogger.info("Disabled Initialization");
        mRunningModules.shutdown(mSystemClock.getCurrentTime());
        mLoopManager.stop();
    }

    @Override
    public void disabledPeriodic() {
    }

    @Override
    public void testInit() {
        mRunningModules.setModules(mDrive);
        mRunningModules.modeInit(mSystemClock.getCurrentTime());
        mRunningModules.periodicInput(mSystemClock.getCurrentTime());
        mRunningModules.checkModule(mSystemClock.getCurrentTime());

        mLoopManager.start();
    }

    @Override
    public void testPeriodic() {

//        mRunningModules.periodicInput(mSystemClock.getCurrentTime());
//        mRunningModules.update(mSystemClock.getCurrentTime());
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
