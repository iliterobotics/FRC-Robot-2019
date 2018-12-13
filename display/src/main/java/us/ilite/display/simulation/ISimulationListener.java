package us.ilite.display.simulation;

import us.ilite.common.lib.geometry.Pose2d;

public interface ISimulationListener {

    void update(double pTimeStamp, Pose2d pCurrentPose);

}
