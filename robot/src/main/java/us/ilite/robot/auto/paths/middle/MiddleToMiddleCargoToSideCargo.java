package us.ilite.robot.auto.paths.middle;

import java.util.Arrays;
import java.util.List;

import com.team254.lib.geometry.Pose2d;
import com.team254.lib.geometry.Rotation2d;

import us.ilite.robot.auto.paths.FieldElementLocations;
import us.ilite.robot.auto.paths.StartingPoses;

/**
 * This auto places 1 hatch on the cargo ship's middle port and one hatch on the side of the cargo ship.
 */
public class MiddleToMiddleCargoToSideCargo {
    
    // End pose of robot @ middle left hatch
    public static final Pose2d kMiddleLeftHatchFromStart = new Pose2d(FieldElementLocations.kCargoShipMiddleLeftHatch, Rotation2d.fromDegrees(0.0));
    // Turn towards loading station
    public static final Rotation2d kTurnToLoadingStationFromMiddleLeftHatch = Rotation2d.fromDegrees(180.0);
    // End pose of robot @ loading station from middle left hatch
    public static final Pose2d kLoadingStationFromMiddleLeftHatch = new Pose2d(FieldElementLocations.kLoadingStation, Rotation2d.fromDegrees(180.0));
    // End pose of robot @ left cargo ship side hatch from loading station
    public static final Pose2d kCargoShipSideLeftHatchFromLoadingStation = new Pose2d(FieldElementLocations.kCargoShipSideLeftHatch, Rotation2d.fromDegrees(-135.0));
    // Turn towards left cargo ship side
    public static final Rotation2d kTurnToLeftCargoShipSideHatch = Rotation2d.fromDegrees(180.0);

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

    // Drive (also probably in reverse) to the cargo ship side hatch
    public static final List<Pose2d> kLoadingStationToSideCargo = Arrays.asList(
        new Pose2d(kLoadingStationFromMiddleLeftHatch.getTranslation(), kTurnToLeftCargoShipSideHatch),
        kCargoShipSideLeftHatchFromLoadingStation
    );

}