package us.ilite.display.simulation;

import com.team254.lib.geometry.Pose2d;

/**
 * Contains information we need to draw robot on the screen.
 * This is a stopgap solution until we integrate the Codex into this.
 */
public class SimData {

    public final Pose2d current_pose, target_pose;

    public SimData(Pose2d pCurrent_pose, Pose2d pTarget_pose) {
        current_pose = pCurrent_pose;
        target_pose = pTarget_pose;
    }

}
