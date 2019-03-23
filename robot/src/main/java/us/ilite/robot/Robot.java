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
import us.ilite.robot.driverinput.DriverInput;
import us.ilite.robot.loops.LoopManager;
import us.ilite.robot.modules.*;

public class Robot extends TimedRobot {

    private ILog mLogger = Logger.createLog(this.getClass());

    private LoopManager mLoopManager = new LoopManager(SystemSettings.kControlLoopPeriod);
    // It sure would be convenient if we could reduce this to just a LoopManager...Will have to test timing of Codex first
    private LoopManager mLoopManagerx = new LoopManager(SystemSettings.kControlLoopPeriod);
    private ModuleList mRunningModules = new ModuleList();

    private Clock mClock = new Clock();
    private Data mData = new Data();
    private Timer initTimer = new Timer();
    private final SystemSettings mSettings = new SystemSettings();
    private CSVLogger mCSVLogger = new CSVLogger(mData);

    private PowerDistributionPanel pdp = new PowerDistributionPanel(SystemSettings.kPowerDistPanelAddress);


    // Module declarations here
    private CommandManager mAutonomousCommandManager = new CommandManager();
    private CommandManager mTeleopCommandManager = new CommandManager();
    private DriveController mDriveController = new DriveController(new HenryProfile());

    private Drive mDrive = new Drive(mData, mDriveController);
    private FourBar mFourBar = new FourBar( mData );
    private Elevator mElevator = new Elevator(mData);
    private Intake mIntake = new Intake(mData);
    private CargoSpit mCargoSpit = new CargoSpit(mData);
    private HatchFlower mHatchFlower = new HatchFlower();
    private Limelight mLimelight = new Limelight(mData);
    private VisionGyro mVisionGyro = new VisionGyro(mData);
    private PneumaticIntake mPneumaticIntake = new PneumaticIntake(mData);

    private LEDControl mLEDControl = new LEDControl( mIntake, mElevator, mHatchFlower, mCargoSpit, mLimelight);

    private DriverInput mDriverInput = new DriverInput( mDrive, mElevator, mHatchFlower, mIntake, mPneumaticIntake, mCargoSpit, mLimelight, mData, mTeleopCommandManager, mAutonomousCommandManager, mFourBar, false  );

    private TrajectoryGenerator mTrajectoryGenerator = new TrajectoryGenerator(mDriveController);
    private AutonomousRoutines mAutonomousRoutines = new AutonomousRoutines(mTrajectoryGenerator, mDrive, mElevator,
            mIntake, mCargoSpit, mHatchFlower, mLimelight, mVisionGyro, mData);
    private MatchMetadata mMatchMeta = null;

    private PerfTimer mClockUpdateTimer = new PerfTimer();

    @Override
    public void robotInit() {
        //look for practice robot config:
        AbstractSystemSettingsUtils.loadPracticeSettings(mSettings);

        // Init the actual robot
        initTimer.reset();
        initTimer.start();
        Logger.setLevel(ELevel.WARN);
        mLogger.info("Starting Robot Initialization...");

        mSettings.writeToNetworkTables();

//        new Thread(new DSConnectInitThread()).start();
        // Init static variables and get singleton instances first

        ICodexTimeProvider provider = new ICodexTimeProvider() {
            public long getTimestamp() {
                return (long) mClock.getCurrentTimeInNanos();
            }
        };
        CodexMetadata.overrideTimeProvider(provider);

        // // Init the actual robot
        // initTimer.reset();
        // initTimer.start();

        // mSettings.writeToNetworkTables();

        // // Logger.setLevel(ELevel.INFO);
        // Logger.setLevel(ELevel.ERROR);
        // mLogger.info("Starting Robot Initialization...");

        // mSettings.writeToNetworkTables();

        mRunningModules.setModules();

        try {
            mAutonomousRoutines.generateTrajectories();
        } catch(Exception e) {
            mLogger.exception(e);
        }

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
        initMatchMetadata(); // TODO - move this to a DS connection thread
        initTimer.reset();
        initTimer.start();
        mLogger.info("Starting Autonomous Initialization...");

        mSettings.loadFromNetworkTables();

        // Init modules after commands are set
        mRunningModules.setModules(mDriverInput, mAutonomousCommandManager, mTeleopCommandManager, mHatchFlower, mPneumaticIntake, mCargoSpit, mElevator, mLimelight, mLEDControl);
        mRunningModules.modeInit(mClock.getCurrentTime());
        mRunningModules.periodicInput(mClock.getCurrentTime());

        mLoopManager.setRunningLoops(mLimelight, mDrive);
        mLoopManager.start();

//        mAutonomousCommandManager.startCommands(mAutonomousRoutines.getDefault());

        mData.registerCodices();
//        mCSVLogger.start(); // Start csv logging

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

        mRunningModules.setModules(mDriverInput, mTeleopCommandManager, mElevator, mHatchFlower, /*mIntake,*/ mCargoSpit, mPneumaticIntake, mFourBar);
        mRunningModules.modeInit(mClock.getCurrentTime());
        mRunningModules.periodicInput(mClock.getCurrentTime());

        mLoopManager.setRunningLoops(mLimelight, mDrive);
        mLoopManager.start();

//        mCSVLogger.start(); // start csv logging
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
        mCSVLogger.stop(); // stop csv logging
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
//        mData.sendCodicesToNetworkTables();
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
