package us.ilite.robot.auto.paths;

import us.ilite.common.lib.trajectory.TrajectoryGenerator;

public abstract class AutoPath {

    protected final TrajectoryGenerator mTrajectoryGenerator;

    public AutoPath(TrajectoryGenerator pTrajectoryGenerator) {
        mTrajectoryGenerator = pTrajectoryGenerator;
    }

}
