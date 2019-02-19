package us.ilite.robot.auto.paths;

import us.ilite.common.lib.trajectory.TrajectoryGenerator;
import us.ilite.robot.commands.ICommand;

public abstract class AutoSequence {

    protected final TrajectoryGenerator mTrajectoryGenerator;

    public AutoSequence(TrajectoryGenerator pTrajectoryGenerator) {
        mTrajectoryGenerator = pTrajectoryGenerator;
    }

    public abstract ICommand[] generateSequence();


}
