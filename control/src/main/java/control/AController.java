package control;

import us.ilite.common.lib.geometry.Pose2d;
import us.ilite.common.lib.geometry.Pose2dWithCurvature;
import us.ilite.common.lib.physics.ChassisState;
import us.ilite.common.lib.physics.DifferentialDrive;
import us.ilite.common.lib.trajectory.TrajectoryIterator;
import us.ilite.common.lib.trajectory.timing.TimedState;

public abstract class AController {

    protected DifferentialDrive mDriveModel;

    protected Pose2d mError;


    public AController(DifferentialDrive pDriveModel) {
        mDriveModel = pDriveModel;
    }

    public abstract DriveOutput update(TrajectoryIterator<TimedState<Pose2dWithCurvature>> pCurrentTrajectory,
                                       TimedState<Pose2dWithCurvature> pSetpoint,
                                        DifferentialDrive.DriveDynamics pDynamics,
                                        ChassisState pPrevVelocity,
                                        Pose2d pCurrentState,
                                        double pDt);

    public Pose2d getError() {
        return mError;
    }

}
