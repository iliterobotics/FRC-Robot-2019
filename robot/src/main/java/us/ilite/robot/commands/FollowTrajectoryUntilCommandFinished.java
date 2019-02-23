package us.ilite.robot.commands;

import com.team254.lib.geometry.Pose2dWithCurvature;
import com.team254.lib.trajectory.Trajectory;
import com.team254.lib.trajectory.timing.TimedState;
import us.ilite.common.Data;
import us.ilite.robot.modules.Drive;

public class FollowTrajectoryUntilCommandFinished extends FollowTrajectory {

    private ICommand mCommand;

    public FollowTrajectoryUntilCommandFinished(Trajectory<TimedState<Pose2dWithCurvature>> pTrajectory, Drive pDrive, boolean pResetPose, ICommand mCommand) {
        super(pTrajectory, pDrive, pResetPose);
        this.mCommand = mCommand;
    }

    @Override
    public void init(double pNow) {
        super.init(pNow);
    }

    @Override
    public boolean update(double pNow) {
        return super.update(pNow) || mCommand.update(pNow);
    }

    @Override
    public void shutdown(double pNow) {
        super.shutdown(pNow);
    }

}
