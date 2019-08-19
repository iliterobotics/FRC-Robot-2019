package us.ilite.robot;

import java.util.List;

import com.flybotix.hfr.codex.Codex;
import com.flybotix.hfr.codex.CodexMetadata;
import com.flybotix.hfr.codex.ICodexTimeProvider;
import com.flybotix.hfr.util.log.ELevel;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import us.ilite.common.Data;
import us.ilite.common.config.AbstractSystemSettingsUtils;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.lib.control.DriveController;
import us.ilite.common.lib.trajectory.TrajectoryGenerator;
import us.ilite.common.lib.util.PerfTimer;
import us.ilite.common.types.MatchMetadata;
import us.ilite.common.types.sensor.EPowerDistPanel;
import us.ilite.lib.drivers.Clock;
import us.ilite.lib.drivers.GetLocalIP;
import us.ilite.lib.drivers.VisionGyro;
import us.ilite.robot.auto.AutonomousRoutines;
import us.ilite.robot.commands.CharacterizeDrive;
import us.ilite.robot.driverinput.DriverInput;
import us.ilite.robot.loops.LoopManager;
import us.ilite.robot.modules.*;

public class Robot extends TimedRobot {

    private final ILog mLogger = Logger.createLog(this.getClass());

    private final LoopManager mLoopManager = new LoopManager(SystemSettings.kControlLoopPeriod);
    private final ModuleList mRunningModules = new ModuleList();

    private final Timer initTimer = new Timer();
    private final Clock mClock = new Clock();
    private final Data mData = new Data();
    private final SystemSettings mSettings = new SystemSettings();
    private final PowerDistributionPanel pdp = new PowerDistributionPanel(SystemSettings.kPowerDistPanelAddress);
    private final DriveController mDriveController = new DriveController(new HenryProfile());

    // Module declarations here
    private final CommandManager mAutonomousCommandManager = new CommandManager().setManagerTag("Autonomous Manager");
    private final CommandManager mTeleopCommandManager = new CommandManager().setManagerTag("Teleop Manager");
    private final Drive mDrive = new Drive(mData, mDriveController);
    private final FourBar mFourBar = new FourBar( mData );
//    private final Elevator mElevator = new Elevator(mData);
    private final Intake mIntake = new Intake(mData);
//    private final CargoSpit mCargoSpit = new CargoSpit(mData);
//    private final HatchFlower mHatchFlower = new HatchFlower(mData);
    private final Limelight mLimelight = new Limelight(mData);
    private final VisionGyro mVisionGyro = new VisionGyro(mData);
    private final LEDControl mLEDControl = new LEDControl(mDrive, /* mCargoSpit,*/  mFourBar, mLimelight, mData);
    private final DriverInput mDriverInput = new DriverInput( mDrive,  mIntake, /* mCargoSpit,*/ mLimelight, mData, mTeleopCommandManager, mAutonomousCommandManager, mFourBar, false  );

    private final TrajectoryGenerator mTrajectoryGenerator = new TrajectoryGenerator(mDriveController);
    private final AutonomousRoutines mAutonomousRoutines = new AutonomousRoutines(mTrajectoryGenerator, mDrive,
            mIntake, /*mCargoSpit,*/ mLimelight, mVisionGyro, mData);
    private MatchMetadata mMatchMeta = null;

    private final PerfTimer mClockUpdateTimer = new PerfTimer();

    @Override
    public void robotInit() {
        initMatchMetadata();
        mData.addMatchMetadata(mMatchMeta);
        //look for practice robot config:
        AbstractSystemSettingsUtils.loadPracticeSettings(mSettings);

        // Init the actual robot
        initTimer.reset();
        initTimer.start();
        Logger.setLevel(ELevel.WARN);
        mLogger.info("Starting Robot Initialization...");

        // Load practice bot settings if this is the practice bot
        AbstractSystemSettingsUtils.loadPracticeSettings(mSettings);
        // Push all values in SystemSettings to NT
        mSettings.writeToNetworkTables();

//        new Thread(new DSConnectInitThread()).start();
        // Init static variables and get singleton instances first
        ICodexTimeProvider provider = new ICodexTimeProvider() {
            public long getTimestamp() {
                return (long) mClock.getCurrentTimeInNanos();
            }
        };
        CodexMetadata.overrideTimeProvider(provider);

        // Clear out running modules
        mRunningModules.setModules();
        CargoSpitSingle.getInstance().setData( mData );
        ElevatorSingle.getInstance().setData( mData );
        PneumaticSingle.getInstance().setData( mData );
        HatchFlowerSingle.getInstance().setData( mData );

        // Generate trajectories on power-on on there's no delay when autonomous is started
        try {
            mAutonomousRoutines.generateTrajectories();
        } catch(Exception e) {
            mLogger.exception(e);
        }

        // Handle telemetry initialization
        mData.registerCodices();
        LiveWindow.disableAllTelemetry();

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
        initTimer.reset();
        initTimer.start();
        mLogger.info("Starting Autonomous Initialization...");

        initMatchMetadata(); // TODO - move this to a DS connection thread

        mSettings.loadFromNetworkTables();

        // Init modules after commands are set
        mRunningModules.setModules(mDriverInput, mAutonomousCommandManager, mTeleopCommandManager, HatchFlowerSingle.getInstance(), /*mIntake,*/ CargoSpitSingle.getInstance(), PneumaticSingle.getInstance(), mFourBar/*, mLEDControl*/, ElevatorSingle.getInstance());
        mRunningModules.modeInit(mClock.getCurrentTime());
        mRunningModules.periodicInput(mClock.getCurrentTime());

        mLoopManager.setRunningLoops(mLimelight, mDrive);
        mLoopManager.start();

//        mAutonomousCommandManager.startCommands(new CharacterizeDrive(mDrive, false, true));

        initTimer.stop();
        mLogger.info("Autonomous initialization finished. Took: ", initTimer.get(), " seconds");
    }

    @Override
    public void autonomousPeriodic() {
        commonPeriodic();
    }

    @Override
    public void teleopInit() {
        initMatchMetadata();

        mSettings.loadFromNetworkTables();

        mRunningModules.setModules(mDriverInput, mTeleopCommandManager, HatchFlowerSingle.getInstance(), /*mIntake,*/ CargoSpitSingle.getInstance(), PneumaticSingle.getInstance(), mFourBar, mLEDControl, ElevatorSingle.getInstance());
        mRunningModules.modeInit(mClock.getCurrentTime());
        mRunningModules.periodicInput(mClock.getCurrentTime());

        mLoopManager.setRunningLoops(mLimelight, mDrive);
        mLoopManager.start();

    }

    @Override
    public void teleopPeriodic() {
        commonPeriodic();
//        mData.sendCodices();
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

    private void commonPeriodic() {
        double start = Timer.getFPGATimestamp();
        for(Codex c : mData.mAllCodexes) {
            c.reset();
        }
        EPowerDistPanel.map(mData.pdp, pdp);
        mRunningModules.periodicInput(mClock.getCurrentTime());
        mRunningModules.update(mClock.getCurrentTime());
        mData.sendCodicesToNetworkTables();
        SmartDashboard.putNumber("common_periodic_dt", Timer.getFPGATimestamp() - start);
    }

    private void initMatchMetadata() {
        if (mMatchMeta == null) {
            mMatchMeta = new MatchMetadata();
            int gid = mMatchMeta.hash;
            for (Codex c : mData.mAllCodexes) {
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

    private class DSConnectInitThread implements Runnable {

        @Override
        public void run() {

            while(!DriverStation.getInstance().isDSAttached()) {
                try {
                    mLogger.error("Waiting on Robot <--> DS Connection...");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            List<String> ips = GetLocalIP.getAllIps();
            mData.initCodexSender(ips);
        }
    }
}
