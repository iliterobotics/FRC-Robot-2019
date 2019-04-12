package us.ilite.robot.auto.paths;

import us.ilite.common.Data;
import us.ilite.common.lib.trajectory.TrajectoryGenerator;
import us.ilite.lib.drivers.VisionGyro;
import us.ilite.robot.commands.ICommand;
import us.ilite.robot.modules.*;

public abstract class AutoSequence {

    protected final TrajectoryGenerator mTrajectoryGenerator;
    protected final Data mData;
    protected final Drive mDrive;
    protected final HatchFlower mHatchFlower;
    protected final PneumaticIntake mPneumaticIntake;
    protected final CargoSpit mCargoSpit;
    protected final Elevator mElevator;
    protected final Limelight mLimelight;
    protected final VisionGyro mVisionGyro;

    public AutoSequence(TrajectoryGenerator mTrajectoryGenerator, Data mData, Drive mDrive, HatchFlower mHatchFlower, PneumaticIntake mPneumaticIntake, CargoSpit mCargoSpit, Elevator mElevator, Limelight mLimelight, VisionGyro mVisionGyro) {
        this.mTrajectoryGenerator = mTrajectoryGenerator;
        this.mData = mData;
        this.mDrive = mDrive;
        this.mHatchFlower = mHatchFlower;
        this.mPneumaticIntake = mPneumaticIntake;
        this.mCargoSpit = mCargoSpit;
        this.mElevator = mElevator;
        this.mLimelight = mLimelight;
        this.mVisionGyro = mVisionGyro;
    }

    public abstract ICommand[] generateCargoSequence();

    public abstract ICommand[] generateHatchSequence();


}
