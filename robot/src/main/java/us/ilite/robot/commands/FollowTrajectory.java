package us.ilite.robot.commands;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import com.team254.lib.geometry.Pose2d;
import com.team254.lib.geometry.Pose2dWithCurvature;
import com.team254.lib.geometry.Translation2d;
import com.team254.lib.trajectory.Trajectory;
import com.team254.lib.trajectory.timing.TimedState;
import edu.wpi.first.wpilibj.Timer;
import us.ilite.common.Data;
import us.ilite.robot.modules.Drive;

public class FollowTrajectory implements ICommand {

    private final ILog mLog = Logger.createLog(FollowTrajectory.class);

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
        mLog.warn("Starting trajectory.");
        mDrive.setPathFollowing();
        mDrive.setPath(mTrajectory, mResetPose);
        if(mResetPose) {
            mDrive.setHeading(mTrajectory.getFirstState().state().getRotation());
        }
    }

    @Override
    public boolean update(double pNow) {

        Pose2d current = mDrive.getDriveController().getCurrentPose();
        Pose2d setpoint = mDrive.getDriveController().getDriveMotionPlanner().mSetpoint.state().getPose();

        Data.kSmartDashboard.putDouble("Heading", current.getRotation().getDegrees());
        Data.kSmartDashboard.putDouble("X", current.getTranslation().x());
        Data.kSmartDashboard.putDouble("Y", current.getTranslation().y());
        Data.kSmartDashboard.putDouble("Target Heading", setpoint.getRotation().getDegrees());
        Data.kSmartDashboard.putDouble("Target X", setpoint.getTranslation().x());
        Data.kSmartDashboard.putDouble("Target Y", setpoint.getTranslation().y());

        if(mDrive.getDriveController().isDone()) {
            Pose2d error = mDrive.getDriveController().getDriveMotionPlanner().error();
            mLog.warn("Trajectory finished.");
            mLog.warn("Cross-track error: ", error);
            mLog.warn("Along-track error: ", error.getTranslation().norm());
            return true;
        }
        return false;
    }

    @Override
    public void shutdown(double pNow) {
        mDrive.setNormal();
    }

}
