package us.ilite.display.simulation;

import java.util.ArrayList;
import java.util.List;

import com.team254.lib.geometry.Pose2d;
import com.team254.lib.geometry.Pose2dWithCurvature;
import com.team254.lib.trajectory.Trajectory;
import com.team254.lib.trajectory.timing.TimedState;
import com.team254.lib.util.ReflectingCSVWriter;

import com.team254.frc2018.planners.DriveMotionPlanner;
import us.ilite.robot.modules.Drive;

public class DriveSimulation {
    
    ReflectingCSVWriter<Pose2d> mOdometryWriter;
    ReflectingCSVWriter<DriveMotionPlanner> mTrajectoryWriter;
    
    private Drive mDrive;
    private final double kDt;
    double time = 0.0;

    List<ISimulationListener> mSimulationListeners = new ArrayList<>();

    public DriveSimulation(Drive pDrive, ReflectingCSVWriter<Pose2d> pOdometryWriter, ReflectingCSVWriter<DriveMotionPlanner> pTrajectoryWriter, double pDt) {
        mDrive = pDrive;
        mOdometryWriter = pOdometryWriter;
        mTrajectoryWriter = pTrajectoryWriter;
        kDt = pDt;
    }

    // TODO Drive doesn't support rotations quite yet
    // public double driveTrajectory(Trajectory<TimedState<Rotation2d>> pTrajectoryToDrive) {
    //     double startTime = time;

    //     mDriveController.setRotationTrajectory(pTrajectoryToDrive);

    //     simulate();

    //     System.out.println("Trajectory time: " + (time - startTime));

    //     return time - startTime;
    // }

    public double driveTrajectory(Trajectory<TimedState<Pose2dWithCurvature>> pTrajectoryToDrive, boolean pResetPoseToTrajectoryStart) {
        double startTime = time;

        mDrive.modeInit(startTime);
        mDrive.setPath(pTrajectoryToDrive, pResetPoseToTrajectoryStart);

        simulate(3.5);

        System.out.println("Trajectory time: " + (time - startTime));

        return time - startTime;
    }

    private void simulate(double pDurationSeconds) {

        for(; time < pDurationSeconds; time += kDt) {
            mDrive.getSimClock().setTime(time);
            mDrive.periodicInput(time);
            mDrive.loop(time);
            mDrive.periodicOutput(time);

            Pose2d currentPose = mDrive.getDriveController().getRobotStateEstimator().getRobotState().getLatestFieldToVehiclePose();

            mTrajectoryWriter.add(mDrive.getDriveController().getDriveMotionPlanner());
            mOdometryWriter.add(currentPose);

            mTrajectoryWriter.flush();
            mOdometryWriter.flush();

            mSimulationListeners.forEach(l -> l.update(time, currentPose));
        }
        
        mDrive.shutdown(time);
    }

    public void addListener(ISimulationListener pListener) {
        mSimulationListeners.add(pListener);
    }

    public void setPose(Pose2d pRobotPose) {
        mDrive.getDriveController().getRobotStateEstimator().reset(0.0, pRobotPose);
    }

}
