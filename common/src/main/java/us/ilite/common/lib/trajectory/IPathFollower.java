package us.ilite.common.lib.trajectory;


import us.ilite.common.lib.geometry.Pose2d;
import us.ilite.common.lib.geometry.Twist2d;

public interface IPathFollower {
    public Twist2d steer(Pose2d current_pose);

    public boolean isDone();
}
