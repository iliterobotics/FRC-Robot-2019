package us.ilite.display.simulation;

import com.team254.lib.geometry.Pose2d;

public interface ISimulationListener {

    void update(double pTimeStamp, Pose2d pCurrentPose);

}
