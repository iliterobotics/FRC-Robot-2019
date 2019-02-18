package us.ilite.robot.auto.paths.middle;

import us.ilite.common.Data;
import us.ilite.common.lib.trajectory.TrajectoryGenerator;
import us.ilite.robot.auto.paths.AutoSequence;
import us.ilite.robot.commands.ICommand;
import us.ilite.robot.modules.Drive;
import us.ilite.robot.modules.Limelight;

public class MiddleToMiddleCargoToSideCargo extends AutoSequence {

    private final Drive mDrive;
    private final Limelight mLimelight;

    public MiddleToMiddleCargoToSideCargo( TrajectoryGenerator pTrajectoryGenerator, Drive pDrive, Limelight pLimelight, Data mData) {
        super(pTrajectoryGenerator);
        this.mDrive = pDrive;
        this.mLimelight = pLimelight;
    }

    @Override
    public ICommand[] generateCargoSequence() {
        return new ICommand[0];
    }

    @Override
    public ICommand[] generateHatchSequence() {
        return new ICommand[]{
                //TODO Create this
        };
    }
}