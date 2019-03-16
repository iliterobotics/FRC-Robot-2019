package us.ilite.robot.auto.paths;

import com.team254.lib.geometry.Translation2d;

/**
 * Holds (x, y) locations for field elements relevant to autonomous pathing.
 * Naming always assumes that the robot is starting on the LEFT.
 */
public class FieldElementLocations {
    //------------------------------
    // Cargo ship scoring locations
    //------------------------------

    // Two middle hatches
    public static final Translation2d kCargoShipMiddleLeftHatch = new Translation2d((14.5 * 12.0) + 52.0 - RobotDimensions.kFrontToCenter, 155.0);
    public static final Translation2d kCargoShipMiddleRightHatch = new Translation2d();

    // Three side hatches - naming assumes you are facing the three hatches
    public static final Translation2d kCargoShipSideLeftHatch = new Translation2d();
    public static final Translation2d kCargoShipSideMiddleHatch = new Translation2d();
    public static final Translation2d kCargoShipSideRightHatch = new Translation2d();

    //-----------------
    // Loading Station
    //-----------------
    public static final Translation2d kLoadingStation = new Translation2d(0.0, 2.0 * 12.0);

    //--------
    // Rocket
    //--------
    // Naming assumes you are facing the front of the rocket
    public static final Translation2d kRocketLeftHatch = new Translation2d();
    public static final Translation2d kRocketMiddleHatch = new Translation2d();
    public static final Translation2d kRocketRightHatch = new Translation2d();


}
