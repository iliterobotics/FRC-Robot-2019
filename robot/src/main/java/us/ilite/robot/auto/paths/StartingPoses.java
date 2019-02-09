package us.ilite.robot.auto.paths;

import com.team254.lib.geometry.Pose2d;
import com.team254.lib.geometry.Rotation2d;

public class StartingPoses {

    public static final Pose2d kMiddleStart = new Pose2d();
    public static final Pose2d kSideStart = new Pose2d(4.0 * 12.0 + RobotDimensions.kBackToCenter, 11.5 * 12.0 + RobotDimensions.kSideToCenter, Rotation2d.fromDegrees(0.0));

}
