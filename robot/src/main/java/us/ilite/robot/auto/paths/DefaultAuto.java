package us.ilite.robot.auto.paths;

import us.ilite.common.Data;
import us.ilite.robot.commands.*;
import us.ilite.robot.modules.Drive;

public class DefaultAuto {

    private final Data mData;
    private final Drive mDrive;

    
    public DefaultAuto ( Data pData, Drive pDrive ) {
        mData = pData;
        mDrive = pDrive;
    }

    public ICommand[] generateDefaultSequence() {
        return new ICommand[] {
                new DriveStraight(mDrive, mData, DriveStraight.EDriveControlMode.PERCENT_OUTPUT, FieldElementLocations.kHabLineDistance)
        };
    }

}
