package us.ilite.robot.commands;

import us.ilite.common.Data;
import us.ilite.lib.drivers.IMU;
import us.ilite.robot.modules.Drive;

public class DriveStraightVision extends CommandQueue {

    private IMU mInitialImu;

    public DriveStraightVision(Drive pDrive, IMU pImu, Data pData, DriveStraight.EDriveControlMode pDriveControlMode, double pDistanceToDrive) {
        mInitialImu = pDrive.getDriveHardware().getImu();
        setCommands(
                new FunctionalCommand(() -> pDrive.getDriveHardware().setImu(pImu)),
                new DriveStraight(pDrive, pData, pDriveControlMode, pDistanceToDrive),
                new FunctionalCommand(() -> pDrive.getDriveHardware().setImu(mInitialImu))
        );
    }

}
