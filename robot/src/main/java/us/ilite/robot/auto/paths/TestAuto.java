package us.ilite.robot.auto.paths;

import com.team254.lib.geometry.Pose2d;
import com.team254.lib.geometry.Rotation2d;

import java.util.Arrays;
import java.util.List;

public class TestAuto {

    private static final Pose2d kOffset = new Pose2d(0.0, 27.0 * 6.0, Rotation2d.fromDegrees(0.0));

    public static final Pose2d kStartPose = new Pose2d(0.0, 0.0, Rotation2d.fromDegrees(0.0));
    public static final Pose2d kEndPose = new Pose2d(8.0 * 12.0, -8.0 * 12.0, Rotation2d.fromDegrees(0.0));

    public static final List<Pose2d> kPath = Arrays.asList(kStartPose, kEndPose);
}
