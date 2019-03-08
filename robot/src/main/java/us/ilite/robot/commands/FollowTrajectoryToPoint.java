package us.ilite.robot.commands;

import com.team254.lib.geometry.Pose2d;
import com.team254.lib.geometry.Pose2dWithCurvature;
import com.team254.lib.trajectory.Trajectory;
import com.team254.lib.trajectory.timing.CentripetalAccelerationConstraint;
import com.team254.lib.trajectory.timing.TimedState;
import com.team254.lib.trajectory.timing.TimingConstraint;
import us.ilite.common.lib.trajectory.TrajectoryGenerator;
import us.ilite.robot.auto.AutonomousRoutines;
import us.ilite.robot.modules.Drive;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Generates a trajectory to a point on-the-fly and then follows it. Trajectory generation uses a separate thread
 * so that we don't unnecessarily block execution.
 */
public class FollowTrajectoryToPoint implements ICommand {

    private Drive mDrive;
    private TrajectoryGenerator mTrajectoryGenerator;

    private boolean mIsReversed;
    private Pose2d mTargetPose;
    private ExecutorService mTrajectoryGenerationThread = Executors.newSingleThreadExecutor();
    private Future<Trajectory<TimedState<Pose2dWithCurvature>>> mTrajectoryGenerationTask;
    private Trajectory<TimedState<Pose2dWithCurvature>> mTrajectory = null;

    public FollowTrajectoryToPoint(Drive mDrive, TrajectoryGenerator mTrajectoryGenerator, boolean mIsReversed, Pose2d mTargetPose) {
        this.mDrive = mDrive;
        this.mTrajectoryGenerator = mTrajectoryGenerator;
        this.mIsReversed = mIsReversed;
        this.mTargetPose = mTargetPose;
    }

    @Override
    public void init(double pNow) {
        /*
         Submit generateTrajectory() to be run in a separate thread. We could just call this on the main
         thread (in init()), but this would break things like ParallelCommand that depend on executing a method
         on a list of objects.
         */
        mTrajectoryGenerationTask = mTrajectoryGenerationThread.submit(this::generateTrajectory);
    }

    @Override
    public boolean update(double pNow) {

        // The trajectory should (hopefully) only ever be null once
        if(mTrajectoryGenerationTask.isDone() && mTrajectory == null) {
            try {
                mTrajectory = mTrajectoryGenerationTask.get();
                mDrive.setPathFollowing();
                mDrive.setPath(mTrajectory, false);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        /*
         If there's no trajectory given to the drive controller, we can't expect an accurate indication
         of whether the trajectory is complete.
         */
        if(mTrajectory != null) {
            return mDrive.getDriveController().isDone();
        }

        return false;

    }

    @Override
    public void shutdown(double pNow) {
        mDrive.setNormal();
    }

    /**
     * @return The trajectory from the robot's current location to an arbitrary pose.
     */
    private Trajectory<TimedState<Pose2dWithCurvature>> generateTrajectory() {
        Pose2d currentPose = mDrive.getDriveController().getCurrentPose();
        List<Pose2d> waypoints = Arrays.asList(currentPose, mTargetPose);
        return mTrajectoryGenerator.generateTrajectory(mIsReversed, waypoints, AutonomousRoutines.kDefaultTrajectoryConstraints);
    }

}
