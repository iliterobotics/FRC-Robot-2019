package us.ilite.robot.hardware;

import us.ilite.common.lib.geometry.Rotation2d;
import us.ilite.robot.modules.DriveMessage;

public interface IDriveHardware extends IHardware {

    void set(DriveMessage pDriveMessage);

    Rotation2d getHeading();

    double getLeftInches();
    double getRightInches();

    double getLeftVelInches();
    double getRightVelInches();

    int getLeftVelTicks();
    int getRightVelTicks();

    double getLeftCurrent();
    double getRightCurrent();

    double getLeftVoltage();
    double getRightVoltage();

}
