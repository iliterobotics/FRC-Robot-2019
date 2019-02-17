package us.ilite.robot;

import java.util.Arrays;
import java.util.List;

import com.flybotix.hfr.codex.Codex;
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
import com.team254.lib.trajectory.TrajectoryUtil;
import com.team254.lib.trajectory.timing.*;

import com.team254.lib.util.Util;
import us.ilite.common.Data;
import us.ilite.common.lib.control.DriveController;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import us.ilite.common.Data;
import us.ilite.common.lib.trajectory.TrajectoryGenerator;
import us.ilite.common.lib.util.PerfTimer;
import us.ilite.common.types.drive.EDriveData;
import us.ilite.lib.drivers.GetLocalIP;
import us.ilite.robot.auto.paths.TestAuto;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.io.Network;
import us.ilite.common.lib.control.DriveController;
import us.ilite.common.lib.trajectory.TrajectoryGenerator;
import us.ilite.common.types.MatchMetadata;
import us.ilite.lib.drivers.Clock;
import us.ilite.robot.commands.CharacterizeDrive;
import us.ilite.robot.commands.CommandQueue;
import us.ilite.robot.auto.paths.TestAuto;
import us.ilite.robot.commands.CommandQueue;
import us.ilite.robot.commands.TurnToDegree;
import us.ilite.lib.drivers.GetLocalIP;
import us.ilite.robot.auto.paths.TestAuto;
import us.ilite.robot.commands.CommandQueue;
import us.ilite.robot.commands.TurnToDegree;
import us.ilite.robot.commands.CharacterizeDrive;
import us.ilite.robot.commands.CommandQueue;
import us.ilite.robot.commands.FollowTrajectory;
import us.ilite.robot.driverinput.DriverInput;
import us.ilite.robot.loops.LoopManager;
import us.ilite.robot.modules.Arm;
import us.ilite.robot.modules.BasicArm;
import us.ilite.robot.modules.MotionMagicArm;
import us.ilite.robot.modules.Drive;
import us.ilite.robot.modules.HatchFlower;
import us.ilite.robot.modules.Intake;
import us.ilite.robot.modules.Limelight;
import us.ilite.robot.modules.Elevator;
import us.ilite.robot.modules.ModuleList;
import us.ilite.robot.modules.Superstructure;
import us.ilite.common.lib.control.DriveController;
import us.ilite.common.lib.control.PIDGains;
import us.ilite.common.lib.control.PIDController;

public class Robot extends TimedRobot {

    private ILog mLogger = Logger.createLog(this.getClass());

    // It sure would be convenient if we could reduce this to just a LoopManager...Will have to test timing of Codex first
    private LoopManager mLoopManager = new LoopManager(SystemSettings.kControlLoopPeriod);
    private ModuleList mRunningModules = new ModuleList();
    private CommandQueue mCommandQueue = new CommandQueue();

    private Clock mClock = new Clock();
    private Data mData = new Data();
    private Timer initTimer = new Timer();
    private SystemSettings mSettings = new SystemSettings();


    // Module declarations here
    private Superstructure mSuperstructure = new Superstructure();
    private DriveController mDriveController = new DriveController(new StrongholdProfile());
    private Drive mDrive = new Drive(mData, mDriveController);
    private Elevator mElevator = new Elevator(mData);
    private HatchFlower mHatchFlower = new HatchFlower();
    private Intake mIntake = new Intake(mData);

    private Arm mArm = new BasicArm();
    // private Arm mArm = new MotionMagicArm();

    private DriverInput mDriverInput = new DriverInput(mDrive, mElevator, mHatchFlower, mIntake, mSuperstructure, mData, mArm);
    
    private Limelight mLimelight = new Limelight();

    private Trajectory<TimedState<Pose2dWithCurvature>> trajectory;

    private MatchMetadata mMatchMeta = null;

    private PerfTimer mClockUpdateTimer = new PerfTimer();


    @Override
    public void robotInit() {
        // Init static variables and get singleton instances first
        Network.getInstance();
        mLogger.info("Netstat determined a driver station IP of ", GetLocalIP.getIp());

        ICodexTimeProvider provider = new ICodexTimeProvider() {
            public long getTimestamp() {
                return (long) mClock.getCurrentTimeInNanos();
            }
        };
        CodexMetadata.overrideTimeProvider(provider);

        // Init the actual robot
        initTimer.reset();
        initTimer.start();

        mSettings.writeToNetworkTables();

        // Logger.setLevel(ELevel.INFO);
        Logger.setLevel(ELevel.ERROR);
        mLogger.info("Starting Robot Initialization...");

        mSettings.writeToNetworkTables();

        mRunningModules.setModules();

        TrajectoryGenerator mTrajectoryGenerator = new TrajectoryGenerator(mDriveController);
        List<TimingConstraint<Pose2dWithCurvature>> kTrajectoryConstraints = Arrays.asList(new CentripetalAccelerationConstraint(40.0));
        trajectory = mTrajectoryGenerator.generateTrajectory(false, TestAuto.kPath, kTrajectoryConstraints, 100.0, 40.0, 12.0);
        trajectory = TrajectoryUtil.mirrorTimed(trajectory);


        mSettings.writeToNetworkTables();

        initTimer.stop();
        mLogger.info("Robot initialization finished. Took: ", initTimer.get(), " seconds");
    }

    /**
     * This contains code run in ALL robot modes.
     * It's also important to note that this runs AFTER mode-specific code
     */
    @Override
    public void robotPeriodic() {
        mClock.cycleEnded();
    }

    @Override
    public void autonomousInit() {
        initMatchMetadata(); // TODO - move this to a DS connection thread
        initTimer.reset();
        initTimer.start();
        mLogger.info("Starting Autonomous Initialization...");

        mSettings.loadFromNetworkTables();

        // Init modules after commands are set
        mRunningModules.setModules(mSuperstructure);
        mRunningModules.modeInit(mClock.getCurrentTime());
        mRunningModules.periodicInput(mClock.getCurrentTime());

        mLoopManager.setRunningLoops(mDrive);
        mLoopManager.start();

//        mSuperstructure.startCommands(new CharacterizeDrive(mDrive, false, false));
        mSuperstructure.startCommands(new FollowTrajectory(trajectory, mDrive, true));

        initTimer.stop();
        mLogger.info("Autonomous initialization finished. Took: ", initTimer.get(), " seconds");
    }

    @Override
    public void autonomousPeriodic() {
        mRunningModules.periodicInput(mClock.getCurrentTime());
        mRunningModules.update(mClock.getCurrentTime());
    }

    @Override
    public void teleopInit() {
        initMatchMetadata();
        mRunningModules.setModules(mDriverInput, mLimelight, mIntake, mHatchFlower, mElevator);

        mSettings.loadFromNetworkTables();
        mRunningModules.modeInit(mClock.getCurrentTime());
        mRunningModules.periodicInput(mClock.getCurrentTime());

        mLoopManager.setRunningLoops(mDrive);
        mLoopManager.start();
        mData.registerCodices();
    }

    @Override
    public void teleopPeriodic() {
        mRunningModules.periodicInput(mClock.getCurrentTime());
        mRunningModules.update(mClock.getCurrentTime());
        mData.sendCodices();
    }

    @Override
    public void disabledInit() {
        mLogger.info("Disabled Initialization");
        mRunningModules.shutdown(mClock.getCurrentTime());
        mLoopManager.stop();
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


    }

    private void initMatchMetadata() {
        if (mMatchMeta == null) {
            mMatchMeta = new MatchMetadata();
            int gid = mMatchMeta.hash;
            for (Codex c : mData.mLoggedCodexes) {
                c.meta().setGlobalId(gid);
            }
        }
    }

    public String toString() {

        String mRobotMode = "Unknown";
        String mRobotEnabledDisabled = "Unknown";
        double mNow = Timer.getFPGATimestamp();

        if (this.isAutonomous()) {
            mRobotMode = "Autonomous";
        }
        if (this.isOperatorControl()) {
            mRobotMode = "Operator Control";
        }
        if (this.isTest()) {
            mRobotEnabledDisabled = "Test";
        }

        if (this.isEnabled()) {
            mRobotEnabledDisabled = "Enabled";
        }
        if (this.isDisabled()) {
            mRobotEnabledDisabled = "Disabled";
        }

        return String.format("State: %s\tMode: %s\tTime: %s", mRobotEnabledDisabled, mRobotMode, mNow);

    }

}
