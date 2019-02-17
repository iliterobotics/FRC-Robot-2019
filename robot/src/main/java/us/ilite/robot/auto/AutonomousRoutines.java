package us.ilite.robot.auto;

import com.team254.lib.geometry.Pose2dWithCurvature;
import com.team254.lib.trajectory.timing.CentripetalAccelerationConstraint;
import com.team254.lib.trajectory.timing.TimingConstraint;
import us.ilite.common.Data;
import us.ilite.common.lib.trajectory.TrajectoryGenerator;
import us.ilite.robot.auto.paths.middle.MiddleToMiddleCargoToSideRocket;
import us.ilite.robot.commands.*;
import us.ilite.robot.modules.Drive;
import us.ilite.robot.modules.Limelight;

import java.util.Arrays;
import java.util.List;

public class AutonomousRoutines {

    public static final List<TimingConstraint<Pose2dWithCurvature>> kTrajectoryConstraints = Arrays.asList(new CentripetalAccelerationConstraint(40.0));
    public static final double kMaxVel = 100.0;
    public static final double kMaxAccel = 40.0;
    public static final double kMaxVoltage = 12.0;

    private TrajectoryGenerator mTrajectoryGenerator;

    private Data mData;
    private Drive mDrive;
    private Limelight mLimelight;

    private MiddleToMiddleCargoToSideRocket mMiddleToMiddleCargoToSideRocket;
    private ICommand[] mMiddleToMiddleCargoToSideRocketSequence;

    public AutonomousRoutines(TrajectoryGenerator pTrajectoryGenerator, Limelight pLimelight, Data pData, Drive pDrive) {
        mTrajectoryGenerator = pTrajectoryGenerator;
        mData = pData;
        mDrive = pDrive;
        mLimelight = pLimelight;

        mMiddleToMiddleCargoToSideRocket = new MiddleToMiddleCargoToSideRocket(pTrajectoryGenerator, pDrive, mLimelight);
    }

    public void generateTrajectories() {
        mMiddleToMiddleCargoToSideRocketSequence = mMiddleToMiddleCargoToSideRocket.generateSequence();
    }

    public ICommand[] getDefault() {
        return mMiddleToMiddleCargoToSideRocketSequence;
    }

}
