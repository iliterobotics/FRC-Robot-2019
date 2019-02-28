package us.ilite.robot.auto.paths.left;

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
import us.ilite.robot.auto.paths.middle.MiddleToMiddleCargoToSideCargo;
import us.ilite.robot.commands.*;
import us.ilite.robot.modules.Drive;
import us.ilite.robot.modules.Limelight;

import java.util.Arrays;
import java.util.List;

public class LeftToMiddleCargoToSideRocket extends AutoSequence {

    private final Data mData;
    private final Drive mDrive;
    private final Limelight mLimelight;
    private final VisionGyro mVisionGyro;

    public LeftToMiddleCargoToSideRocket(TrajectoryGenerator pTrajectoryGenerator, Data mData, Drive mDrive, Limelight mLimelight, VisionGyro mVisionGyro) {
        super(pTrajectoryGenerator);
        this.mData = mData;
        this.mDrive = mDrive;
        this.mLimelight = mLimelight;
        this.mVisionGyro = mVisionGyro;
    }

    // End pose of robot @ middle left hatch
    //TODO Change angle
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

    // Drive (also probably in reverse) to the Middle Side Cargo
    public static final List<Pose2d> kLoadingStationToSideMiddleCargoPath = Arrays.asList(
            new Pose2d(kLoadingStationFromMiddleLeftHatch.getTranslation(), kTurnToLeftRocketHatch),
            kLeftRocketHatchFromLoadingStation
    );

    public Trajectory<TimedState<Pose2dWithCurvature>> getStartToMiddleLeftHatchTrajectory() {
        return mTrajectoryGenerator.generateTrajectory(false, kStartToMiddleLeftHatchPath, AutonomousRoutines.kDefaultTrajectoryConstraints);
    }

    public Trajectory<TimedState<Pose2dWithCurvature>> getMiddleLeftHatchToLoadingStationPath() {
        return mTrajectoryGenerator.generateTrajectory(true, kMiddleLeftHatchToLoadingStationPath, AutonomousRoutines.kDefaultTrajectoryConstraints);
    }

    public Trajectory<TimedState<Pose2dWithCurvature>> getLoadingStationToSideRocketPath() {
        return mTrajectoryGenerator.generateTrajectory(false, kLoadingStationToSideMiddleCargoPath, AutonomousRoutines.kDefaultTrajectoryConstraints);
    }

    @Override
    public ICommand[] generateCargoSequence() {
        return new ICommand[] {
                new FollowTrajectoryUntilCommandFinished(
                        getStartToMiddleLeftHatchTrajectory(),
                        mDrive,
                        true,
                        new WaitForVisionTarget(mData, 3.5)
                ),
                new TargetLock(
                        mDrive,
                        2.0,
                        ETrackingType.TARGET_LEFT,
                        mLimelight, () -> 0.0,
                        false).setTargetLockThrottleProvider(() -> 0.5),
                new DriveStraight(
                        mDrive,
                        mData,
                        DriveStraight.EDriveControlMode.PERCENT_OUTPUT,
                        MiddleToMiddleCargoToSideCargo.kMiddleLeftHatchFromStart.getTranslation().
                                translateBy(StartingPoses.kMiddleStart.getTranslation().inverse()).norm()
                ),
                new Delay(5),
                new FollowTrajectory(
                        getMiddleLeftHatchToLoadingStationPath(),
                        mDrive,
                        true
                ),
                new Delay(5),
                new TurnToDegree(
                        mDrive,
                        Rotation2d.fromDegrees(180.0),
                        10.0,
                        mData
                ),
                new LimelightTargetLock(
                        mDrive,
                        mLimelight,
                        3,
                        ETrackingType.TARGET_LEFT,
                        mLimelight, () -> 0.0,
                        true),
                new DriveStraightVision(
                        mDrive,
                        mVisionGyro,
                        mData,
                        DriveStraight.EDriveControlMode.PERCENT_OUTPUT,
                        12.0 * 4.0)
        };
    }

    @Override
    public ICommand[] generateHatchSequence() {
        return new ICommand[] {
                //TODO Make this
        };
    }

}
