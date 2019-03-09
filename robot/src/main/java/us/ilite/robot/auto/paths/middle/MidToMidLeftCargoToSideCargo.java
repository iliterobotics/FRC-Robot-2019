package us.ilite.robot.auto.paths.middle;

import us.ilite.common.Data;
import us.ilite.common.lib.trajectory.TrajectoryGenerator;
import us.ilite.lib.drivers.VisionGyro;
import us.ilite.robot.auto.paths.AutoSequence;
import us.ilite.robot.commands.ICommand;
import us.ilite.robot.modules.*;

public class MidToMidLeftCargoToSideCargo extends AutoSequence {

    public MidToMidLeftCargoToSideCargo(TrajectoryGenerator mTrajectoryGenerator, Data mData, Drive mDrive, HatchFlower mHatchFlower, PneumaticIntake mPneumaticIntake, CargoSpit mCargoSpit, Elevator mElevator, Limelight mLimelight, VisionGyro mVisionGyro) {
        super(mTrajectoryGenerator, mData, mDrive, mHatchFlower, mPneumaticIntake, mCargoSpit, mElevator, mLimelight, mVisionGyro);
    }

    public ICommand[] generateCargoSequence() {
        return new ICommand[] {
                //TODO
        };
    }

    public ICommand[] generateHatchSequence() {
        return new ICommand[] {
                //TODO Make this
        };
    }

}
