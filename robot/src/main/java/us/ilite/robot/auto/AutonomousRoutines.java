package us.ilite.robot.auto;

import com.team254.lib.geometry.Pose2dWithCurvature;
import com.team254.lib.trajectory.timing.CentripetalAccelerationConstraint;
import com.team254.lib.trajectory.timing.TimingConstraint;
import us.ilite.common.Data;
import us.ilite.common.lib.trajectory.TrajectoryGenerator;
import us.ilite.robot.auto.paths.middle.MiddleToMiddleCargoToSideRocket;
import us.ilite.robot.commands.*;
import us.ilite.robot.modules.*;

import java.util.Arrays;
import java.util.List;

public class AutonomousRoutines {

    public static final List<TimingConstraint<Pose2dWithCurvature>> kTrajectoryConstraints = Arrays.asList(new CentripetalAccelerationConstraint(40.0));
    public static final double kMaxVel = 100.0;
    public static final double kMaxAccel = 40.0;
    public static final double kMaxVoltage = 12.0;

    private TrajectoryGenerator mTrajectoryGenerator;

    private Drive mDrive;
    private Elevator mElevator;
    private Intake mIntake;
    private CargoSpit mCargoSpit;
    private HatchFlower mHatchFlower;
    private Limelight mLimelight;
    private Data mData;

    private MiddleToMiddleCargoToSideRocket mMiddleToMiddleCargoToSideRocket;
    private ICommand[] mMiddleToMiddleCargoToSideRocketSequence;

    public AutonomousRoutines(TrajectoryGenerator pTrajectoryGenerator, Drive pDrive, Elevator pElevator, Intake pIntake, CargoSpit pCargoSpit, HatchFlower pHatchFlower, Limelight pLimelight, Data pData) {
        mTrajectoryGenerator = pTrajectoryGenerator;
        mDrive = pDrive;
        mElevator = pElevator;
        mIntake = pIntake;
        mCargoSpit = pCargoSpit;
        mHatchFlower = pHatchFlower;
        mLimelight = pLimelight;
        mData = pData;

        mMiddleToMiddleCargoToSideRocket = new MiddleToMiddleCargoToSideRocket(mTrajectoryGenerator, mDrive, mLimelight);
    }

    public void generateTrajectories() {
        mMiddleToMiddleCargoToSideRocketSequence = mMiddleToMiddleCargoToSideRocket.generateSequence();
    }

    public ICommand[] getDefault() {
        return mMiddleToMiddleCargoToSideRocketSequence;
    }

}
