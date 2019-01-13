package us.ilite.common.lib.control;

import com.team254.lib.geometry.Pose2d;
import com.team254.lib.geometry.Pose2dWithCurvature;
import com.team254.lib.physics.DifferentialDrive;
import com.team254.lib.physics.DriveOutput;
import com.team254.lib.trajectory.TrajectoryIterator;
import com.team254.lib.trajectory.timing.TimedState;

public abstract class AController {

    protected DifferentialDrive mDriveModel;

    protected Pose2d mError;


    public AController(DifferentialDrive pDriveModel) {
        mDriveModel = pDriveModel;
    }

    public abstract DriveOutput update(TrajectoryIterator<TimedState<Pose2dWithCurvature>> pCurrentTrajectory,
                                       TimedState<Pose2dWithCurvature> pSetpoint,
                                       DifferentialDrive.DriveDynamics pDynamics,
                                       DifferentialDrive.ChassisState pPrevVelocity,
                                       Pose2d pCurrentState,
                                       double pDt);

    public Pose2d getError() {
        return mError;
    }

}
