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
import org.mockito.Mock;
import us.ilite.common.Data;
import us.ilite.common.config.AbstractSystemSettingsUtils;
import us.ilite.common.config.PracticeBotSystemSettings;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.lib.control.DriveController;
import us.ilite.common.lib.odometry.RobotStateEstimator;
import us.ilite.common.lib.trajectory.TrajectoryGenerator;
import us.ilite.common.types.MatchMetadata;
import us.ilite.lib.drivers.Clock;
import us.ilite.lib.drivers.VisionGyro;
import us.ilite.robot.HenryProfile;
import us.ilite.robot.auto.AutonomousRoutines;
import us.ilite.robot.driverinput.DriverInput;
import us.ilite.robot.modules.*;

import java.util.Arrays;
import java.util.List;

public class SimRobot extends SimHarness {

    private static final ILog mLogger = Logger.createLog(SimRobot.class);

    private final ModuleList mRunningModules = new ModuleList();
    
    private final Clock mClock = new Clock().simulated();
    private final Data mData = new Data();
    private final SystemSettings mSettings = new SystemSettings();
    private final DriveController mDriveController = new DriveController(new HenryProfile());

    // Module declarations here
    private final CommandManager mAutonomousCommandManager = new CommandManager().setManagerTag("Autonomous Manager");
    private final CommandManager mTeleopCommandManager = new CommandManager().setManagerTag("Teleop Manager");
    private final Drive mDrive = new Drive(mData, mDriveController, mClock, true);
    
    @Mock private FourBar mFourBar;
    @Mock private Elevator mElevator;
    @Mock private Intake mIntake;
    @Mock private CargoSpit mCargoSpit;
    @Mock private HatchFlower mHatchFlower;
    @Mock private Limelight mLimelight = new Limelight(mData);
    @Mock private VisionGyro mVisionGyro;
    @Mock private PneumaticIntake mPneumaticIntake;
    @Mock private LEDControl mLEDControl;
    @Mock private DriverInput mDriverInput;

    private final TrajectoryGenerator mTrajectoryGenerator = new TrajectoryGenerator(mDriveController);
    private final AutonomousRoutines mAutonomousRoutines = new AutonomousRoutines(mTrajectoryGenerator, mDrive, mElevator,
            mPneumaticIntake, mIntake, mCargoSpit, mHatchFlower, mLimelight, mVisionGyro, mData);
    private MatchMetadata mMatchMeta = null;

    public SimRobot(double pScheduleRate) {
        super(pScheduleRate);
        setClock(mClock);
    }

    public void simInit() {
        Logger.setLevel(ELevel.WARN);

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
        this.setStopCondition(() -> !mAutonomousCommandManager.isRunningCommands());

        mRunningModules.setModules(mAutonomousCommandManager, mDrive);
        mRunningModules.modeInit(mClock.getCurrentTime());

        mAutonomousRoutines.getSequence();
        mAutonomousCommandManager.startCommands();

//        mAutonomousCommandManager.startCommands(
//            new FollowTrajectory(generate(LeftToRocket.kStartToBackRocketPath), mDrive, true),
//                new FollowTrajectory(generate(true, LeftToRocket.kBackRocketToLinePath), mDrive, false),
//                new FollowTrajectory(generate(LeftToRocket.kLineToLoadingStationPath), mDrive, false),
//                new FollowTrajectory(generate(true, LeftToRocket.kLoadingStationToBackRocketPath), mDrive, false),
//                new FollowTrajectory(generate(LeftToRocket.kLineToBackRocketPath), mDrive, false)
//        );

    }

    public void simPeriodic() {
        mRunningModules.periodicInput(mClock.getCurrentTime());
        mRunningModules.update(mClock.getCurrentTime());
//        mData.sendCodices();
      mData.sendCodicesToNetworkTables();
        mClock.cycleEnded();
    }

    public void simShutdown() {
        mAutonomousCommandManager.stopRunningCommands(mClock.getCurrentTime());
        mRunningModules.shutdown(mClock.getCurrentTime());
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
