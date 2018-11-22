package lib.trajectory;


import lib.geometry.Pose2d;
import lib.geometry.Twist2d;

public interface IPathFollower {
    public Twist2d steer(Pose2d current_pose);

    public boolean isDone();
}
