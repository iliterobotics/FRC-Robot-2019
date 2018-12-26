package us.ilite.robot;

import com.flybotix.hfr.util.log.ELevel;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

import control.DriveController;
import control.DriveMotionPlanner;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Timer;
import paths.TrajectoryGenerator;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.lib.geometry.Pose2d;
import us.ilite.common.lib.geometry.Pose2dWithCurvature;
import us.ilite.common.lib.geometry.Rotation2d;
import us.ilite.common.lib.trajectory.Trajectory;
import us.ilite.common.lib.trajectory.timing.CentripetalAccelerationConstraint;
import us.ilite.common.lib.trajectory.timing.TimedState;
import us.ilite.common.lib.trajectory.timing.TimingConstraint;
import us.ilite.common.types.drive.EDriveData;
import us.ilite.lib.drivers.Clock;
import us.ilite.lib.util.SimpleNetworkTable;
import us.ilite.robot.commands.CharacterizeDrive;
import us.ilite.robot.commands.CommandQueue;
import us.ilite.robot.commands.FollowTrajectory;
import us.ilite.robot.driverinput.DriverInput;
import us.ilite.robot.loops.LoopManager;
import us.ilite.robot.modules.Drive;
import us.ilite.robot.modules.ModuleList;

import java.util.Arrays;
import java.util.List;

public class Robot extends IterativeRobot {
    
    private ILog mLogger = Logger.createLog(this.getClass());

    private CommandQueue mCommandQueue = new CommandQueue();

    // It sure would be convenient if we could reduce this to just a LoopManager...Will have to test timing of Codex first
    private LoopManager mLoopManager = new LoopManager(SystemSettings.kControlLoopPeriod);
    private ModuleList mRunningModules = new ModuleList();

    private Clock mClock = new Clock();
    private Data mData = new Data();

    // Module declarations here
    private DriveController mDriveController = new DriveController(new StrongholdProfile(), SystemSettings.kControlLoopPeriod);
    private Drive mDrive = new Drive(mData, mDriveController, mClock);
    private DriverInput mDriverInput = new DriverInput(mDrive, mData);

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
//        mLogger.info(this.toString());

        mClock.cycleEnded();
    }

    @Override
    public void autonomousInit() {
        mapNonModuleInputs();

        mRunningModules.setModules();
        mRunningModules.modeInit(mClock.getCurrentTime());
        mRunningModules.periodicInput(mClock.getCurrentTime());

        mLoopManager.setRunningLoops(mDrive);
        mLoopManager.start();

        mDriveController.setPlannerMode(DriveMotionPlanner.PlannerMode.FEEDBACK);
//        mDriveController.getController().setGains(0.65, 0.175);
        mDriveController.getController().setGains(2.0, 0.7);

//        mDriveController.getController().setGains(0.0, 0.0);
        TrajectoryGenerator mTrajectoryGenerator = new TrajectoryGenerator(mDriveController);
        List<TimingConstraint<Pose2dWithCurvature>> kTrajectoryConstraints = Arrays.asList(new CentripetalAccelerationConstraint(70.0));
        List<Pose2d> waypoints = Arrays.asList(new Pose2d[] {
                new Pose2d(0.0, 0.0, Rotation2d.fromDegrees(0.0)),
                // new Pose2d(7.0 * 12.0, -7.0 * 12.0, Rotation2d.fromDegrees(-90.0))
                new Pose2d(20.0 * 12.0, 0.0, Rotation2d.fromDegrees(0.0))
        });
        Trajectory<TimedState<Pose2dWithCurvature>> trajectory = mTrajectoryGenerator.generateTrajectory(false, waypoints, kTrajectoryConstraints, 60.0, 60.0, 12.0);


        mCommandQueue.setCommands(new FollowTrajectory(trajectory, mDrive, true));
//        mCommandQueue.setCommands(new CharacterizeDrive(mDrive, false, false));

        mCommandQueue.init(mClock.getCurrentTime());
    }

    @Override
    public void autonomousPeriodic() {
        mapNonModuleInputs();

        mRunningModules.periodicInput(mClock.getCurrentTime());
        if(!mCommandQueue.isFinished()) mCommandQueue.update(mClock.getCurrentTime());
        mRunningModules.update(mClock.getCurrentTime());
    }

    @Override
    public void teleopInit() {
        mapNonModuleInputs();

        mRunningModules.setModules(mDriverInput);
        mRunningModules.modeInit(mClock.getCurrentTime());
        mRunningModules.periodicInput(mClock.getCurrentTime());

        mLoopManager.setRunningLoops(mDrive);
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
