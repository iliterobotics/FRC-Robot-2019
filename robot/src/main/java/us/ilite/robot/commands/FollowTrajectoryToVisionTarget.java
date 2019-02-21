package us.ilite.robot.commands;

import com.team254.lib.geometry.Pose2d;
import com.team254.lib.geometry.Rotation2d;
import com.team254.lib.geometry.Translation2d;
import us.ilite.common.Data;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.lib.trajectory.TrajectoryGenerator;
import us.ilite.common.types.ETargetingData;
import us.ilite.robot.modules.Drive;
import us.ilite.robot.modules.Limelight;
import us.ilite.robot.modules.targetData.ITargetDataProvider;

public class FollowTrajectoryToVisionTarget extends CommandQueue {

    private Drive mDrive;
    private Data mData;
    private TrajectoryGenerator mTrajectoryGenerator;

    private FollowTrajectoryToPoint mFollowTrajectoryCommand;

    public FollowTrajectoryToVisionTarget(Drive mDrive, Data mData, TrajectoryGenerator mTrajectoryGenerator) {
        this.mDrive = mDrive;
        this.mData = mData;
        this.mTrajectoryGenerator = mTrajectoryGenerator;

        Translation2d targetPoint = new Translation2d(mData.limelight.get(ETargetingData.calcTargetX), mData.limelight.get(ETargetingData.calcTargetY));
        Rotation2d targetHeading = mDrive.getHeading().rotateBy(Rotation2d.fromDegrees(mData.limelight.get(ETargetingData.tx)));

        setCommands(
                new FollowTrajectoryToPoint(mDrive, mTrajectoryGenerator, false, new Pose2d(targetPoint, targetHeading))
        );

    }

}
