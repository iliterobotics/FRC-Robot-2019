package us.ilite.robot.auto.paths.middle;

import java.util.Arrays;
import java.util.List;

import com.team254.lib.geometry.Pose2d;
import com.team254.lib.geometry.Pose2dWithCurvature;
import com.team254.lib.geometry.Rotation2d;

import com.team254.lib.geometry.Translation2d;
import com.team254.lib.trajectory.Trajectory;
import com.team254.lib.trajectory.timing.TimedState;
import us.ilite.common.lib.trajectory.TrajectoryGenerator;
import us.ilite.common.types.ETrackingType;
import us.ilite.robot.auto.AutonomousRoutines;
import us.ilite.robot.auto.paths.AutoSequence;
import us.ilite.robot.auto.paths.FieldElementLocations;
import us.ilite.robot.auto.paths.RobotDimensions;
import us.ilite.robot.auto.paths.StartingPoses;
import us.ilite.robot.commands.FollowTrajectory;
import us.ilite.robot.commands.ICommand;
import us.ilite.robot.commands.TargetLock;
import us.ilite.robot.modules.Drive;
import us.ilite.robot.modules.Limelight;

/**
 * This auto places 1 hatch on the cargo ship's middle port and one hatch on the side of the rocket.
 */
public class MiddleToMiddleCargoToSideRocket extends AutoSequence {

    private final Drive mDrive;
    private final Limelight mLimelight;

    public MiddleToMiddleCargoToSideRocket(TrajectoryGenerator pTrajectoryGenerator, Drive pDrive, Limelight pLimelight) {
        super(pTrajectoryGenerator);
        mDrive = pDrive;
        mLimelight = pLimelight;
    }

    // End pose of robot @ middle left hatch
    public static final Pose2d kMiddleLeftHatchFromStart = new Pose2d(FieldElementLocations.kCargoShipMiddleLeftHatch.translateBy(new Translation2d(-RobotDimensions.kFrontToCenter, 0.0)), Rotation2d.fromDegrees(0.0));
    // Turn towards loading station
    public static final Rotation2d kTurnToLoadingStationFromMiddleLeftHatch = Rotation2d.fromDegrees(180.0);
    // End pose of robot @ loading station from middle left hatch
    public static final Pose2d kLoadingStationFromMiddleLeftHatch = new Pose2d(FieldElementLocations.kLoadingStation, Rotation2d.fromDegrees(180.0));
    // End pose of robot @ left rocket hatch from loading station
    public static final Pose2d kLeftRocketHatchFromLoadingStation = new Pose2d(FieldElementLocations.kRocketLeftHatch, Rotation2d.fromDegrees(-60.0));
    // Turn towards rocket side
    public static final Rotation2d kTurnToLeftRocketHatch = Rotation2d.fromDegrees(180.0);

    // Drive to the middle of the cargo ship's left-hand port
    public static final List<Pose2d> kStartToMiddleLeftHatchPath = Arrays.asList(
        StartingPoses.kSideStart,
        kMiddleLeftHatchFromStart
    );

    // Drive (probably in reverse) to the loading station
    public static final List<Pose2d> kMiddleLeftHatchToLoadingStationPath = Arrays.asList(
        new Pose2d(kMiddleLeftHatchFromStart.getTranslation(), kTurnToLoadingStationFromMiddleLeftHatch),
        kLoadingStationFromMiddleLeftHatch
    );

    // Drive (also probably in reverse) to the rocket
    public static final List<Pose2d> kLoadingStationToSideRocketPath = Arrays.asList(
        new Pose2d(kLoadingStationFromMiddleLeftHatch.getTranslation(), kTurnToLeftRocketHatch),
        kLeftRocketHatchFromLoadingStation
    );

    public Trajectory<TimedState<Pose2dWithCurvature>> getStartToMiddleLeftHatchTrajectory() {
        return mTrajectoryGenerator.generateTrajectory(false, kStartToMiddleLeftHatchPath, AutonomousRoutines.kTrajectoryConstraints,  100.0, 40.0, 12.0);
    }

    public Trajectory<TimedState<Pose2dWithCurvature>> getMiddleLeftHatchToLoadingStationPath() {
        return mTrajectoryGenerator.generateTrajectory(true, kMiddleLeftHatchToLoadingStationPath, AutonomousRoutines.kTrajectoryConstraints,  100.0, 40.0, 12.0);
    }

    public Trajectory<TimedState<Pose2dWithCurvature>> getLoadingStationToSideRocketPath() {
        return mTrajectoryGenerator.generateTrajectory(false, kLoadingStationToSideRocketPath, AutonomousRoutines.kTrajectoryConstraints,  100.0, 40.0, 12.0);
    }

    @Override
    public ICommand[] generateSequence() {
        return new ICommand[] {
                /*new DriveStraight(mDrive, mData, DriveStraight.EDriveControlMode.PERCENT_OUTPUT,
                        MiddleToMiddleCargoToSideRocket.kMiddleLeftHatchFromStart.getTranslation().translateBy(StartingPoses.kMiddleStart.getTranslation().inverse()).norm()),
                new Delay(5),*/
                /* new FollowTrajectory(getMiddleLeftHatchToLoadingStationPath(), mDrive, true), */
                /*new Delay(5),
                new TurnToDegree(mDrive, Rotation2d.fromDegrees(180.0), 10.0, mData)*/
                new TargetLock(mDrive, 3, ETrackingType.TARGET_LEFT, mLimelight, () -> 0.5, true)
        };
    }

}