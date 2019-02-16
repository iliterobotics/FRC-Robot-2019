package us.ilite.robot.auto;

import com.team254.lib.geometry.Pose2dWithCurvature;
import com.team254.lib.geometry.Rotation2d;
import com.team254.lib.trajectory.Trajectory;
import com.team254.lib.trajectory.timing.CentripetalAccelerationConstraint;
import com.team254.lib.trajectory.timing.TimedState;
import com.team254.lib.trajectory.timing.TimingConstraint;
import us.ilite.common.Data;
import us.ilite.common.lib.trajectory.TrajectoryGenerator;
import us.ilite.robot.auto.paths.StartingPoses;
import us.ilite.robot.auto.paths.middle.MiddleToMiddleCargoToSideRocket;
import us.ilite.robot.commands.DriveStraight;
import us.ilite.robot.commands.FollowTrajectory;
import us.ilite.robot.commands.ICommand;
import us.ilite.robot.commands.TurnToDegree;
import us.ilite.robot.modules.Drive;

import java.util.Arrays;
import java.util.List;

public class AutonomousRoutines {

    public static final List<TimingConstraint<Pose2dWithCurvature>> kTrajectoryConstraints = Arrays.asList(new CentripetalAccelerationConstraint(40.0));
    public static final double kMaxVel = 100.0;
    public static final double kMaxAccel = 40.0;
    public static final double kMaxVoltage = 12.0;

    private TrajectoryGenerator mTrajectoryGenerator;

    private Data mData;
    private Drive mDrive;

    private MiddleToMiddleCargoToSideRocket mMiddleToMiddleCargoToSideRocket;
    private ICommand[] mMiddleToMiddleCargoToSideRocketSequence;

    public AutonomousRoutines(TrajectoryGenerator pTrajectoryGenerator, Data pData, Drive pDrive) {
        mTrajectoryGenerator = pTrajectoryGenerator;
        mData = pData;
        mDrive = pDrive;

        mMiddleToMiddleCargoToSideRocket = new MiddleToMiddleCargoToSideRocket(mTrajectoryGenerator);
    }

    public void generateTrajectories() {
        mMiddleToMiddleCargoToSideRocketSequence = new ICommand[] {
                new DriveStraight(mDrive, mData, DriveStraight.EDriveControlMode.PERCENT_OUTPUT,
                        MiddleToMiddleCargoToSideRocket.kMiddleLeftHatchFromStart.distance(StartingPoses.kMiddleStart)),
                new FollowTrajectory(mMiddleToMiddleCargoToSideRocket.getLoadingStationToSideRocketPath(), mDrive, true),
                new TurnToDegree(mDrive, Rotation2d.fromDegrees(180.0), 10.0, mData)
        };
    }

    public ICommand[] getDefault() {
        return mMiddleToMiddleCargoToSideRocketSequence;
    }

}
