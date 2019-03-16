package us.ilite.robot.auto.paths;

import com.team254.lib.geometry.Pose2d;
import com.team254.lib.geometry.Rotation2d;

/**
 * Defines the starting location of the *center* of the robot as (x, y, theta) coordinates.
 * Units are in inches.
 */
public class StartingPoses {

    public static final Pose2d kMiddleStart = new Pose2d();

    /**
     * X: 4 feet + length from back to center
     * Y: 11.5 feet + length from side of robot to center
     */
    public static final Pose2d kSideStart = new Pose2d((4.0 * 12.0) + RobotDimensions.kBackToCenter, (11.5 * 12.0) + RobotDimensions.kSideToCenter, Rotation2d.fromDegrees(0.0));

}
