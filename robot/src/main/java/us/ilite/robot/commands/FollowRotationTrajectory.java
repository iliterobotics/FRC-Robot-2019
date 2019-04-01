package us.ilite.robot.commands;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import com.team254.lib.geometry.Pose2d;
import com.team254.lib.geometry.Pose2dWithCurvature;
import com.team254.lib.geometry.Rotation2d;
import com.team254.lib.trajectory.Trajectory;
import com.team254.lib.trajectory.timing.TimedState;
import us.ilite.common.Data;
import us.ilite.robot.modules.Drive;

public class FollowRotationTrajectory implements ICommand {

    private final ILog mLog = Logger.createLog(FollowTrajectory.class);

    private Trajectory<TimedState<Rotation2d>> mTrajectory;
    private Drive mDrive;
    private boolean mResetPose;

    public FollowRotationTrajectory(Trajectory<TimedState<Rotation2d>> pTrajectory, Drive pDrive, boolean pResetPose) {
        mTrajectory = pTrajectory;
        mDrive = pDrive;
        mResetPose = pResetPose;
    }

    @Override
    public void init(double pNow) {
        mLog.warn("Starting trajectory.");
        mDrive.setProfilingToHeading();
        mDrive.setRotationProfile(mTrajectory, mResetPose);
        if(mResetPose) {
            mDrive.setHeading(mTrajectory.getFirstState().state().getRotation());
        }
    }

    @Override
    public boolean update(double pNow) {

        Pose2d current = mDrive.getDriveController().getCurrentPose();
        Pose2d setpoint = mDrive.getDriveController().getTargetPose();

        Data.kSmartDashboard.putDouble("Heading", current.getRotation().getDegrees());
        Data.kSmartDashboard.putDouble("Target Heading", setpoint.getRotation().getDegrees());

        if(mDrive.getDriveController().isDone()) {
            Rotation2d error = mDrive.getDriveController().getDriveMotionPlanner().error().getRotation();
            mLog.warn("Trajectory finished.");
            mLog.warn("Heading error: ", error);
            return true;
        }
        return false;
    }

    @Override
    public void shutdown(double pNow) {
        mDrive.setNormal();
    }

}
