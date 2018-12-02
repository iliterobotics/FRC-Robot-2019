package us.ilite.robot.commands;

import us.ilite.common.lib.geometry.Pose2dWithCurvature;
import us.ilite.common.lib.trajectory.Trajectory;
import us.ilite.common.lib.trajectory.timing.TimedState;
import us.ilite.robot.modules.TrajectoryFollower;

public class FollowTrajectory implements ICommand {

    private Trajectory<TimedState<Pose2dWithCurvature>> mTrajectory;
    private TrajectoryFollower mTrajectoryFollower;
    private boolean mResetPose;

    private boolean mWriteToCsv = false;

    public FollowTrajectory(Trajectory<TimedState<Pose2dWithCurvature>> pTrajectory, TrajectoryFollower pTrajectoryFollower, boolean pResetPose) {
        mTrajectory = pTrajectory;
        mTrajectoryFollower = pTrajectoryFollower;
        mResetPose = pResetPose;
    }

    @Override
    public void init(double pNow) {
        mTrajectoryFollower.getDriveController().setTrajectory(mTrajectory, mResetPose);
        mTrajectoryFollower.enable();
    }

    @Override
    public boolean update(double pNow) {
        if(mTrajectoryFollower.getDriveController().isDone()) {
            return true;
        }
        return false;
    }

    @Override
    public void shutdown(double pNow) {
        mTrajectoryFollower.disable();
    }

    public FollowTrajectory setWriteToCsv() {
        mWriteToCsv = true;
        return this;
    }

}
