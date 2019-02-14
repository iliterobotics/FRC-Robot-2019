package us.ilite.robot.auto.paths;

import com.team254.lib.geometry.Pose2d;
import com.team254.lib.geometry.Rotation2d;

import java.util.Arrays;
import java.util.List;

public class TestAuto {
    public static final Pose2d kStartPose = new Pose2d(FieldElementLocations.kCargoShipMiddleLeftHatch, Rotation2d.fromDegrees(180.0));
    public static final Pose2d kEndPose = new Pose2d(FieldElementLocations.kLoadingStation, Rotation2d.fromDegrees(180.0));
//    public static final Pose2d kStartPose = new Pose2d(0.0, 0.0, Rotation2d.fromDegrees(0.0));
//    public static final Pose2d kEndPose = new Pose2d(10.0 * 12.0, -5.0 * 12.0, Rotation2d.fromDegrees(0.0));
    public static final List<Pose2d> kPath = Arrays.asList(kStartPose, kEndPose);
}
