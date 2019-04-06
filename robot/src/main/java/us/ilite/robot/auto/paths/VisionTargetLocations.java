package us.ilite.robot.auto.paths;

import com.team254.lib.geometry.Pose2d;
import com.team254.lib.geometry.Rotation2d;
import com.team254.lib.geometry.Translation2d;

public enum VisionTargetLocations {

    // TODO Fill in degree values
    RIGHT_LOADING_STATION(0.0, 300.0, 0.0),
    LEFT_LOADING_STATION(0.0, 28.0, 0.0),

    RIGHT_FRONT_ROCKET(213.0, 307.0, 0.0),
    RIGHT_BACK_ROCKET(243.0, 307.0, 0.0),

    LEFT_FRONT_ROCKET(213.0, 20.0, 0.0),
    LEFT_BACK_ROCKET(243.0, 20.0, 0.0);

    public final Pose2d kLocation;

    VisionTargetLocations(double x, double y, double degrees) {
        this.kLocation = new Pose2d(x, y, Rotation2d.fromDegrees(degrees));
    }

}
