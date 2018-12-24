package us.ilite.robot.commands;

import us.ilite.common.lib.geometry.Pose2dWithCurvature;
import us.ilite.common.lib.trajectory.Trajectory;
import us.ilite.common.lib.trajectory.timing.TimedState;
import us.ilite.robot.modules.Drive;

public class FollowTrajectory implements ICommand {

    private Trajectory<TimedState<Pose2dWithCurvature>> mTrajectory;
    private Drive mDrive;
    private boolean mResetPose;

    public FollowTrajectory(Trajectory<TimedState<Pose2dWithCurvature>> pTrajectory, Drive pDrive, boolean pResetPose) {
        mTrajectory = pTrajectory;
        mDrive = pDrive;
        mResetPose = pResetPose;
    }

    @Override
    public void init(double pNow) {
        mDrive.followPath(mTrajectory, mResetPose);
    }

    @Override
    public boolean update(double pNow) {
        if(mDrive.getDriveController().isDone()) {
            return true;
        }
        return false;
    }

    @Override
    public void shutdown(double pNow) {

    }

}
