package us.ilite.robot.auto;

import com.team254.lib.geometry.Pose2dWithCurvature;
import com.team254.lib.trajectory.timing.CentripetalAccelerationConstraint;
import com.team254.lib.trajectory.timing.TimingConstraint;
import us.ilite.common.Data;
import us.ilite.common.lib.trajectory.TrajectoryConstraints;
import us.ilite.common.lib.trajectory.TrajectoryGenerator;
import us.ilite.lib.drivers.VisionGyro;
import us.ilite.robot.auto.paths.middle.MiddleToMiddleCargoToSideRocket;
import us.ilite.robot.commands.*;
import us.ilite.robot.modules.*;

import java.util.Arrays;
import java.util.List;

public class AutonomousRoutines {

    public static final TrajectoryConstraints kDefaultTrajectoryConstraints = new TrajectoryConstraints(
            100.0,
            40.0,
            12.0,
            new CentripetalAccelerationConstraint(20.0)
    );

    private TrajectoryGenerator mTrajectoryGenerator;

    private Drive mDrive;
//    private Elevator mElevator;
    private Intake mIntake;
//    private CargoSpit mCargoSpit;
    private HatchFlower mHatchFlower;
    private Limelight mLimelight;
    private VisionGyro mVisionGyro;
    private Data mData;

    private MiddleToMiddleCargoToSideRocket mMiddleToMiddleCargoToSideRocket;
    private ICommand[] mMiddleToMiddleCargoToSideRocketSequence;

    public AutonomousRoutines(TrajectoryGenerator mTrajectoryGenerator, Drive mDrive, Intake mIntake, /**/ HatchFlower mHatchFlower, Limelight mLimelight, VisionGyro mVisionGyro, Data mData) {
        this.mTrajectoryGenerator = mTrajectoryGenerator;
        this.mDrive = mDrive;
//        this.mElevator = mElevator;
        this.mIntake = mIntake;
//        this.mCargoSpit = mCargoSpit;
        this.mHatchFlower = mHatchFlower;
        this.mLimelight = mLimelight;
        this.mVisionGyro = mVisionGyro;
        this.mData = mData;

        this.mMiddleToMiddleCargoToSideRocket = new MiddleToMiddleCargoToSideRocket(mTrajectoryGenerator, mData, mDrive, mHatchFlower, mLimelight, mVisionGyro);
    }

    public void generateTrajectories() {
        mMiddleToMiddleCargoToSideRocketSequence = mMiddleToMiddleCargoToSideRocket.generateSequence();
    }

    public ICommand[] getDefault() {
        return mMiddleToMiddleCargoToSideRocketSequence;
    }

}
