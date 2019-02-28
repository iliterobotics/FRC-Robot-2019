package us.ilite.robot.auto.paths.middle;

import java.util.Arrays;
import java.util.List;

import com.team254.lib.geometry.Pose2d;
import com.team254.lib.geometry.Pose2dWithCurvature;
import com.team254.lib.geometry.Rotation2d;

import com.team254.lib.geometry.Translation2d;
import com.team254.lib.trajectory.Trajectory;
import com.team254.lib.trajectory.timing.TimedState;
import us.ilite.common.Data;
import us.ilite.common.lib.trajectory.TrajectoryGenerator;
import us.ilite.common.types.ETrackingType;
import us.ilite.lib.drivers.VisionGyro;
import us.ilite.robot.auto.AutonomousRoutines;
import us.ilite.robot.auto.paths.AutoSequence;
import us.ilite.robot.auto.paths.FieldElementLocations;
import us.ilite.robot.auto.paths.RobotDimensions;
import us.ilite.robot.auto.paths.StartingPoses;
import us.ilite.robot.commands.*;
import us.ilite.robot.modules.Drive;
import us.ilite.robot.modules.Limelight;

public class MiddleToMiddleCargoToMiddleCargo extends AutoSequence {

    private final Data mData;
    private final Drive mDrive;
    private final Limelight mLimelight;
    private final VisionGyro mVisionGyro;

    public MiddleToMiddleCargoToMiddleCargo(TrajectoryGenerator pTrajectoryGenerator, Data mData, Drive mDrive, Limelight mLimelight, VisionGyro mVisionGyro) {
        super(pTrajectoryGenerator);
        this.mData = mData;
        this.mDrive = mDrive;
        this.mLimelight = mLimelight;
        this.mVisionGyro = mVisionGyro;
    }

    // End pose of robot @ middle left cargo
    public static final Pose2d kMiddleLeftCargoFromStart = new Pose2d(FieldElementLocations.kCargoShipMiddleLeftHatch.translateBy(new Translation2d(-RobotDimensions.kFrontToCenter, 0.0)), Rotation2d.fromDegrees(0.0));
    // Turn towards loading station
    public static final Rotation2d kTurnToLoadingStationFromMiddleLeftHatch = Rotation2d.fromDegrees(180.0);
    // End pose of robot @ loading station from middle left hatch
    public static final Pose2d kLoadingStationFromMiddleLeftHatch = new Pose2d(FieldElementLocations.kLoadingStation, Rotation2d.fromDegrees(180.0));
    // Turn towards middle right cargo
    public static final Rotation2d kTurnToRightMiddleHatchFromLoadingStation = Rotation2d.fromDegrees(180);
    // End pose of robot @ middle right cargo from loading station, tune angle
    public static final Pose2d kMiddleRightCargoFromLoadingStation = new Pose2d(FieldElementLocations.kCargoShipMiddleRightHatch, Rotation2d.fromDegrees(-60));
    //May not need above


    // Drive to the middle of the cargo ship's left-hand port
    public static final List<Pose2d> kStartToMiddleLeftHatchPath = Arrays.asList(
        StartingPoses.kSideStart,
        kMiddleLeftCargoFromStart
    );

     // Drive (probably in reverse) to the loading station
      public static final List<Pose2d> kMiddleLeftHatchToLoadingStationPath = Arrays.asList(
        new Pose2d(kMiddleLeftCargoFromStart.getTranslation(), kTurnToLoadingStationFromMiddleLeftHatch),
        kLoadingStationFromMiddleLeftHatch
    );

    //Drive (reverse?) to middle of the cargo ship's right-hand port
    public static final List<Pose2d> kLoadingStationToRightHatchPath = Arrays.asList(
        new Pose2d(kLoadingStationFromMiddleLeftHatch.getTranslation(), kTurnToRightMiddleHatchFromLoadingStation),
        kMiddleRightCargoFromLoadingStation
    );

    public Trajectory<TimedState<Pose2dWithCurvature>> getStartToMiddleLeftHatchTrajectory() {
        return mTrajectoryGenerator.generateTrajectory(false, kStartToMiddleLeftHatchPath, AutonomousRoutines.kDefaultTrajectoryConstraints);
    }

    public Trajectory<TimedState<Pose2dWithCurvature>> getMiddleLeftHatchToLoadingStationPath() {
        return mTrajectoryGenerator.generateTrajectory(true, kMiddleLeftHatchToLoadingStationPath, AutonomousRoutines.kDefaultTrajectoryConstraints);
    }

    public Trajectory<TimedState<Pose2dWithCurvature>> getLoadingStationToRightCargoPath() {
        return mTrajectoryGenerator.generateTrajectory(false, kLoadingStationToRightHatchPath, AutonomousRoutines.kDefaultTrajectoryConstraints);
    }

    @Override
    public ICommand[] generateCargoSequence() {
        return new ICommand[]{
            new FollowTrajectoryUntilCommandFinished(getStartToMiddleLeftHatchTrajectory(), mDrive, true,
                        new WaitForVisionTarget(mData, 3.5)),
                new TargetLock(mDrive, 2.0, ETrackingType.TARGET_LEFT, mLimelight, () -> 0.0, false).setTargetLockThrottleProvider(() -> 0.5)
        };

    }


    @Override
    public ICommand[] generateHatchSequence() {
        return new ICommand[]{
                //TODO Create this
        };
    }
}