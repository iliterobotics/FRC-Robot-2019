package us.ilite.robot.auto.paths.middle;

import us.ilite.common.Data;
import us.ilite.lib.drivers.VisionGyro;
import us.ilite.robot.commands.ICommand;
import us.ilite.robot.modules.Drive;
import us.ilite.robot.modules.Limelight;

public class MidToMidRightCargoToRocket {

    private final Data mData;
    private final Drive mDrive;
    private final Limelight mLimelight;
    private final VisionGyro mVisionGyro;

    public MidToMidRightCargoToRocket(Data mData, Drive mDrive, Limelight mLimelight, VisionGyro mVisionGyro) {
        this.mData = mData;
        this.mDrive = mDrive;
        this.mLimelight = mLimelight;
        this.mVisionGyro = mVisionGyro;
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
