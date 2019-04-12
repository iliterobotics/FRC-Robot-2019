package us.ilite.robot.auto.paths.right;

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
import us.ilite.robot.modules.*;

public class RightToRocketToRocket extends AutoSequence {

    public RightToRocketToRocket(TrajectoryGenerator mTrajectoryGenerator, Data mData, Drive mDrive, HatchFlower mHatchFlower, PneumaticIntake mPneumaticIntake, CargoSpit mCargoSpit, Elevator mElevator, Limelight mLimelight, VisionGyro mVisionGyro) {
        super(mTrajectoryGenerator, mData, mDrive, mHatchFlower, mPneumaticIntake, mCargoSpit, mElevator, mLimelight, mVisionGyro);
    }

      // End pose of robot @ midLeft right hatch
      public static final Pose2d kMiddleRightHatchFromStart = new Pose2d(FieldElementLocations.kCargoShipMiddleRightHatch.translateBy(new Translation2d(-RobotDimensions.kFrontToCenter, 0.0)), Rotation2d.fromDegrees(0.0));
      // Turn towards loading station
      public static final Rotation2d kTurnToLoadingStationFromMiddleRightHatch = Rotation2d.fromDegrees(180.0);
      // End pose of robot @ loading station from midLeft right hatch
      public static final Pose2d kLoadingStationFromMiddleRightHatch = new Pose2d(FieldElementLocations.kLoadingStation, Rotation2d.fromDegrees(180.0));
      // End pose of robot @ left rocket hatch from loading station
      public static final Pose2d kRightRocketHatchFromLoadingStation = new Pose2d(FieldElementLocations.kRocketRightHatch, Rotation2d.fromDegrees(-60.0));
      // Turn towards rocket side
      public static final Rotation2d kTurnToLeftRocketHatch = Rotation2d.fromDegrees(180.0);
  
      // Drive to the midLeft of the cargo ship's left-hand port
      public static final List<Pose2d> kStartToMiddleRightHatchPath = Arrays.asList(
          StartingPoses.kFarSideStart,
          kMiddleRightHatchFromStart
      );
  
      // Drive (probably in reverse) to the loading station
      public static final List<Pose2d> kMiddleRightHatchToLoadingStationPath = Arrays.asList(
          new Pose2d(kMiddleRightHatchFromStart.getTranslation(), kTurnToLoadingStationFromMiddleRightHatch),
          kLoadingStationFromMiddleRightHatch
      );
  
      // Drive (also probably in reverse) to the rocket
      public static final List<Pose2d> kLoadingStationToSideRocketPath = Arrays.asList(
          new Pose2d(kLoadingStationFromMiddleRightHatch.getTranslation(), kTurnToLeftRocketHatch),
          kRightRocketHatchFromLoadingStation
      );
  
      public Trajectory<TimedState<Pose2dWithCurvature>> getStartToMiddleLeftHatchTrajectory() {
          return mTrajectoryGenerator.generateTrajectory(false, kStartToMiddleRightHatchPath, AutonomousRoutines.kDefaultTrajectoryConstraints);
      }
  
      public Trajectory<TimedState<Pose2dWithCurvature>> getMiddleRightHatchToLoadingStationPath() {
          return mTrajectoryGenerator.generateTrajectory(true, kMiddleRightHatchToLoadingStationPath, AutonomousRoutines.kDefaultTrajectoryConstraints);
      }
  
      public Trajectory<TimedState<Pose2dWithCurvature>> getLoadingStationToSideRocketPath() {
          return mTrajectoryGenerator.generateTrajectory(false, kLoadingStationToSideRocketPath, AutonomousRoutines.kDefaultTrajectoryConstraints);
      }
  
    @Override
    public ICommand[] generateCargoSequence() {
          return new ICommand[]{};
    }

    @Override
    public ICommand[] generateHatchSequence() {
          return new ICommand[]{};
    }
    
}
