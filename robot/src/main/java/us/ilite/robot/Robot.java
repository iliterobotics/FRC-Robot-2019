package us.ilite.robot;

import java.util.Scanner;

import com.flybotix.hfr.util.log.ELevel;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Timer;
import us.ilite.common.config.SystemSettings;
import us.ilite.lib.drivers.Clock;
import us.ilite.robot.commands.CommandQueue;
import us.ilite.robot.loops.LoopManager;
import us.ilite.robot.modules.ExampleModule;
import us.ilite.robot.modules.ModuleList;

public class Robot extends IterativeRobot {
    
    private ILog mLogger = Logger.createLog(this.getClass());

    private CommandQueue mCommandQueue = new CommandQueue();

    // Module declarations here
    private ExampleModule mExampleModule = new ExampleModule();

    // It sure would be convenient if we could reduce this to just a LoopManager...Will have to test timing of Codex first
    private LoopManager mLoopManager = new LoopManager(SystemSettings.CONTROL_LOOP_PERIOD);
    private ModuleList mRunningModules = new ModuleList();

    private Clock mClock = new Clock();
    private Data mData = new Data().simulated();

    @Override
    public void robotInit() {
        Timer initTimer = new Timer();
        initTimer.start();
        Logger.setLevel(ELevel.DEBUG);
        mLogger.info("Starting Robot Initialization...");

        mRunningModules.setModules();

        initTimer.stop();
        mLogger.info("Robot initialization finished. Took: ", initTimer.get(), " seconds");
    }

    /**
     * This contains code run in ALL robot modes.
     * It's also important to note that this runs AFTER mode-specific code
     */
    @Override
    public void robotPeriodic() {
        mLogger.info(this.toString());

        Scanner keyboard = new Scanner(System.in);
        String line = keyboard.nextLine();
        mLogger.debug("You entered: " + line);

        mClock.cycleEnded();
    }

    @Override
    public void autonomousInit() {
        mapNonModuleInputs();

        mRunningModules.setModules(mExampleModule);
        mRunningModules.modeInit(mClock.getCurrentTime());

        mLoopManager.start();
    }

    @Override
    public void autonomousPeriodic() {
        mapNonModuleInputs();

        mRunningModules.periodicInput(mClock.getCurrentTime());
        mCommandQueue.update(mClock.getCurrentTime());
        mRunningModules.update(mClock.getCurrentTime());
    }

    @Override
    public void teleopInit() {
        mapNonModuleInputs();

        mRunningModules.setModules(mExampleModule);
        mRunningModules.modeInit(mClock.getCurrentTime());

        mLoopManager.start();
    }

    @Override
    public void teleopPeriodic() {
        mapNonModuleInputs();

        mRunningModules.periodicInput(mClock.getCurrentTime());
        mRunningModules.update(mClock.getCurrentTime());
    }

    @Override
    public void disabledInit() {
        mRunningModules.shutdown(mClock.getCurrentTime());
        mLoopManager.stop();
    }

    @Override
    public void disabledPeriodic() {

    }

    @Override
    public void testInit() {
        mRunningModules.setModules(mExampleModule);
        mRunningModules.modeInit(mClock.getCurrentTime());

        mLoopManager.start();
    }

    @Override
    public void testPeriodic() {
        mapNonModuleInputs();

        mRunningModules.periodicInput(mClock.getCurrentTime());
        mRunningModules.checkModule(mClock.getCurrentTime());
        mRunningModules.update(mClock.getCurrentTime());
    }

    public void mapNonModuleInputs() {

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
