package us.ilite.display.simulation;

import com.flybotix.hfr.codex.CodexMetadata;
import com.flybotix.hfr.codex.ICodexTimeProvider;
import com.flybotix.hfr.util.log.ELevel;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import com.team254.lib.geometry.Pose2d;
import com.team254.lib.geometry.Pose2dWithCurvature;
import com.team254.lib.geometry.Rotation2d;
import com.team254.lib.trajectory.Trajectory;
import com.team254.lib.trajectory.timing.TimedState;
import us.ilite.common.Data;
import us.ilite.common.config.AbstractSystemSettingsUtils;
import us.ilite.common.config.PracticeBotSystemSettings;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.lib.control.DriveController;
import us.ilite.common.lib.odometry.RobotStateEstimator;
import us.ilite.common.lib.trajectory.TrajectoryGenerator;
import us.ilite.lib.drivers.Clock;
import us.ilite.robot.HenryProfile;
import us.ilite.robot.auto.AutonomousRoutines;
import us.ilite.robot.auto.paths.right.RightToRocketToRocket;
import us.ilite.robot.commands.FollowTrajectory;
import us.ilite.robot.modules.CommandManager;
import us.ilite.robot.modules.Drive;
import us.ilite.robot.modules.ModuleList;

import java.util.Arrays;
import java.util.List;

public class SimRobot extends SimHarness {

    private static final ILog mLogger = Logger.createLog(SimRobot.class);

    public final Data mData = new Data();
    public final Clock mClock = new Clock().simulated();
    public final DriveController mDriveController = new DriveController(new HenryProfile());
    public final TrajectoryGenerator mTrajectoryGenerator = new TrajectoryGenerator(mDriveController);
    public final Drive mDrive = new Drive(mData, mDriveController, mClock, true);
    public final CommandManager mCommandManager = new CommandManager();
    public final ModuleList mModuleList = new ModuleList();

    public SimRobot(double pScheduleRate) {
        super(pScheduleRate);
    }

    public void simInit() {
        Logger.setLevel(ELevel.ERROR);

        // Force use of PracticeBotSystemSettings. This is necessary because the robot code isn't fully ready for NEO units yet.
        AbstractSystemSettingsUtils.copyOverValues(PracticeBotSystemSettings.getInstance(), new SystemSettings());

        ICodexTimeProvider provider = new ICodexTimeProvider() {
            public long getTimestamp() {
                return (long) mClock.getCurrentTimeInNanos();
            }
        };
        CodexMetadata.overrideTimeProvider(provider);
        mData.initCodexSender(Arrays.asList("127.0.0.1"));
        mData.registerCodices();

        mDrive.startCsvLogging();
        this.setStopCondition(() -> !mCommandManager.isRunningCommands());

        mModuleList.setModules(mCommandManager, mDrive);
        mModuleList.modeInit(mClock.getCurrentTime());

        mCommandManager.startCommands(
            new FollowTrajectory(generate(RightToRocketToRocket.kStartToBackRocketPath), mDrive, true),
                new FollowTrajectory(generate(true, RightToRocketToRocket.kBackRocketToLinePath), mDrive, false),
                new FollowTrajectory(generate(RightToRocketToRocket.kLineToLoadingStationPath), mDrive, false),
                new FollowTrajectory(generate(true, RightToRocketToRocket.kLoadingStationToBackRocketPath), mDrive, false),
                new FollowTrajectory(generate(RightToRocketToRocket.kLineToBackRocketPath), mDrive, false)
        );

    }

    public void simPeriodic() {
        mModuleList.periodicInput(mClock.getCurrentTime());
        mModuleList.update(mClock.getCurrentTime());
        mData.sendCodices();
//      mData.sendCodicesToNetworkTables();
        mClock.cycleEnded();
    }

    public void simShutdown() {
        mCommandManager.stopRunningCommands(mClock.getCurrentTime());
        mModuleList.shutdown(mClock.getCurrentTime());
    }

    public Trajectory<TimedState<Pose2dWithCurvature>> generate(List<Pose2d> waypoints) {
        return generate(false, waypoints);
    }

    public Trajectory<TimedState<Pose2dWithCurvature>> generate(boolean reversed, List<Pose2d> waypoints) {
        return mTrajectoryGenerator.generateTrajectory(reversed, waypoints, AutonomousRoutines.kDefaultTrajectoryConstraints);
    }

    public Trajectory<TimedState<Rotation2d>> generate(double initialHeading, double finalHeading ) {
        return mTrajectoryGenerator.generateTurnInPlaceTrajectory(initialHeading, finalHeading,0.0, AutonomousRoutines.kDefaultTrajectoryConstraints);
    }

    public Trajectory<TimedState<Rotation2d>> generate(Rotation2d initialHeading, Rotation2d finalHeading ) {
        return mTrajectoryGenerator.generateTurnInPlaceTrajectory(initialHeading, finalHeading,0.0,  AutonomousRoutines.kDefaultTrajectoryConstraints);
    }

    public RobotStateEstimator getRobotStateEstimator() {
        return mDriveController.getRobotStateEstimator();
    }

}
