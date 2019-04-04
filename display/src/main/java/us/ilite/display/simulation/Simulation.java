package us.ilite.display.simulation;

import java.util.List;

import com.flybotix.hfr.util.log.ELevel;
import com.flybotix.hfr.util.log.Logger;
import com.team254.lib.geometry.Pose2d;
import com.team254.lib.geometry.Pose2dWithCurvature;
import com.team254.lib.geometry.Rotation2d;
import com.team254.lib.trajectory.Trajectory;
import com.team254.lib.trajectory.timing.TimedState;
import com.team254.lib.util.ReflectingCSVWriter;

import us.ilite.common.Data;
import us.ilite.common.lib.RobotProfile;
import us.ilite.common.lib.control.DriveController;
import com.team254.frc2018.planners.DriveMotionPlanner;
import us.ilite.common.lib.odometry.RobotStateEstimator;
import us.ilite.common.lib.trajectory.TrajectoryGenerator;
import us.ilite.display.simulation.ui.FieldWindow;
import us.ilite.lib.drivers.Clock;
import us.ilite.robot.HenryProfile;
import us.ilite.robot.auto.AutonomousRoutines;
import us.ilite.robot.auto.paths.middle.MiddleToMiddleCargoToSideRocket;
import us.ilite.robot.commands.FollowRotationTrajectory;
import us.ilite.robot.commands.FollowTrajectory;
import us.ilite.robot.modules.CommandManager;
import us.ilite.robot.modules.Drive;

public class Simulation {

    private final double kDt;

    private final Data mData;
    private final Clock mClock;
    private final DriveController mDriveController;
    private final TrajectoryGenerator mTrajectoryGenerator;
    private final Drive mDrive;
    private final UiUpdater mUiUpdater;
    private final CommandManager mCommandManager;

    private final ModuleSim mSim;

    private final FieldWindow mFieldWindow;

    public Simulation(RobotProfile pRobotProfile, FieldWindow pFieldWindow, double pDt) {
        kDt = pDt;
        mData = new Data();
        mClock = new Clock().simulated();
        mFieldWindow = pFieldWindow;
        mDriveController = new DriveController(pRobotProfile);
        mTrajectoryGenerator = new TrajectoryGenerator(mDriveController);
        mDrive = new Drive(mData, mDriveController, mClock, true);
        mCommandManager = new CommandManager();
        mUiUpdater = new UiUpdater(mData, pFieldWindow);

        mSim = new ModuleSim(0.01, mCommandManager, mDrive, mUiUpdater);
    }

    public void simulate() {

        Logger.setLevel(ELevel.DEBUG);

//        mDrive.startCsvLogging();
        mCommandManager.startCommands(
                new FollowTrajectory(generate(MiddleToMiddleCargoToSideRocket.kStartToMiddleLeftHatchPath), mDrive, true),
                new FollowTrajectory(generate(true, MiddleToMiddleCargoToSideRocket.kMiddleLeftHatchToLoadingStationPath), mDrive,false),
                new FollowRotationTrajectory(generate(MiddleToMiddleCargoToSideRocket.kMiddleLeftHatchFromStart.getRotation(), MiddleToMiddleCargoToSideRocket.kLoadingStationFromMiddleLeftHatch.getRotation()), mDrive,false),
                new FollowTrajectory(generate(true, MiddleToMiddleCargoToSideRocket.kLoadingStationToSideRocketSetupPath), mDrive, false)
        );

        mSim.start();

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
