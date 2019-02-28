package us.ilite.robot.auto.paths.middle;

import com.team254.lib.geometry.Pose2d;
import com.team254.lib.geometry.Rotation2d;
import com.team254.lib.geometry.Translation2d;
import us.ilite.common.Data;
import us.ilite.common.lib.trajectory.TrajectoryGenerator;
import us.ilite.lib.drivers.VisionGyro;
import us.ilite.robot.auto.paths.AutoSequence;
import us.ilite.robot.auto.paths.FieldElementLocations;
import us.ilite.robot.auto.paths.RobotDimensions;
import us.ilite.robot.commands.ICommand;
import us.ilite.robot.modules.Drive;
import us.ilite.robot.modules.Limelight;

public class MiddleToMiddleCargoToSideCargo extends AutoSequence {

    private final Data mData;
    private final Drive mDrive;
    private final Limelight mLimelight;
    private final VisionGyro mVisionGyro;

    public MiddleToMiddleCargoToSideCargo(TrajectoryGenerator pTrajectoryGenerator, Data mData, Drive mDrive, Limelight mLimelight, VisionGyro mVisionGyro) {
        super(pTrajectoryGenerator);
        this.mData = mData;
        this.mDrive = mDrive;
        this.mLimelight = mLimelight;
        this.mVisionGyro = mVisionGyro;
    }

    // End pose of robot @ middle left hatch
    public static final Pose2d kMiddleLeftHatchFromStart = new Pose2d(FieldElementLocations.kCargoShipMiddleLeftHatch.translateBy(new Translation2d(-RobotDimensions.kFrontToCenter, 0.0)), Rotation2d.fromDegrees(0.0));
    // Turn towards loading station
    public static final Rotation2d kTurnToLoadingStationFromMiddleLeftHatch = Rotation2d.fromDegrees(180.0);
    // End pose of robot @ loading station from middle left hatch
    public static final Pose2d kLoadingStationFromMiddleLeftHatch = new Pose2d(FieldElementLocations.kLoadingStation, Rotation2d.fromDegrees(180.0));
    // End pose of robot @ left rocket hatch from loading station
    public static final Pose2d kLeftRocketHatchFromLoadingStation = new Pose2d(FieldElementLocations.kRocketLeftHatch, Rotation2d.fromDegrees(-60.0));
    // Turn towards rocket side
    public static final Rotation2d kTurnToLeftRocketHatch = Rotation2d.fromDegrees(180.0);


    @Override
    public ICommand[] generateCargoSequence() {



        return new ICommand[0];
    }

    @Override
    public ICommand[] generateHatchSequence() {
        return new ICommand[]{
                //TODO Create this
        };
    }
}