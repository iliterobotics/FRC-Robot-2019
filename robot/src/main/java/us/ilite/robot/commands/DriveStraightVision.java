package us.ilite.robot.commands;

import com.team254.lib.geometry.Rotation2d;
import us.ilite.common.Data;
import us.ilite.common.config.SystemSettings;
import us.ilite.lib.drivers.IMU;
import us.ilite.robot.modules.Drive;

public class DriveStraightVision extends CommandQueue {

    private IMU mInitialImu;

    public DriveStraightVision(Drive pDrive, IMU pImu, Data pData, DriveStraight.EDriveControlMode pDriveControlMode, double pDistanceToDrive) {
        mInitialImu = pDrive.getDriveHardware().getImu();
        setCommands(
                new FunctionalCommand(() -> pDrive.getDriveHardware().setImu(pImu)),
                new DriveStraight(pDrive, pData, pDriveControlMode, pDistanceToDrive)
                        .setTargetHeading(Rotation2d.fromDegrees(0.0))
                        .setHeadingGains(SystemSettings.kTargetAngleLockGains),
                new FunctionalCommand(() -> pDrive.getDriveHardware().setImu(mInitialImu))
        );
    }

}
