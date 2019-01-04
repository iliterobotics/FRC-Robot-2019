package us.ilite.robot.auto.paths;

import com.team254.lib.geometry.Pose2d;
import com.team254.lib.geometry.Rotation2d;
import com.team254.lib.geometry.Translation2d;

import java.util.Arrays;
import java.util.List;

public class NearScaleAuto {

    public static final Pose2d kGoingToScale = new Pose2d(170.0, 50.0, Rotation2d.fromDegrees(0.0));
    public static final Pose2d kAtScale = new Pose2d(FieldElementPoses.kNearScale, Rotation2d.fromDegrees(22.5));
    public static final Pose2d kTurnFromScaleToFirstCube = new Pose2d(kAtScale.getTranslation(), Rotation2d.fromDegrees(170.0));
    public static final Pose2d kScaleToFirstCube = new Pose2d(FieldElementPoses.kFirstCube, Rotation2d.fromDegrees(170.0));
    public static final Pose2d kTurnFromFirstCubeToScale = new Pose2d(kScaleToFirstCube.getTranslation(), Rotation2d.fromDegrees(-10.0));
    public static final Pose2d kFirstCubeToScale = new Pose2d(FieldElementPoses.kNearScale.translateBy(new Translation2d(0.0, 5.0)), Rotation2d.fromDegrees(0.0));

    public static final List<Pose2d> kToScalePath = Arrays.asList(StartingPoses.kSideStart, kGoingToScale, kAtScale);
    public static final List<Pose2d> kScaleToFirstCubePath = Arrays.asList(kTurnFromScaleToFirstCube, kScaleToFirstCube);
    public static final List<Pose2d> kFirstCubeToScalePath = Arrays.asList(kTurnFromFirstCubeToScale, kFirstCubeToScale);

}
