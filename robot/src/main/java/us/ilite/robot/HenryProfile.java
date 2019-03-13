package us.ilite.robot;

import us.ilite.common.config.SystemSettings;
import us.ilite.common.lib.RobotProfile;
import us.ilite.common.lib.util.Units;

public class HenryProfile implements RobotProfile {

    @Override
    public double getLeftVoltPerAccel() {
        return 0.01150625538259917;
    }

    @Override
    public double getLeftVoltPerSpeed() {
        return 0.24744231405368078;
    }

    @Override
    public double getLeftFrictionVoltage() {
        return 0.6096948673174356;
    }

    @Override
    public double getRightVoltPerAccel() {
        return 0.013188926536306073;
    }

    @Override
    public double getRightVoltPerSpeed() {
        return 0.26596262541467847;
    }

    @Override
    public double getRightFrictionVoltage() {
        return 0.44698232783836034;
    }

    @Override
    public double getWheelRadiusMeters() {
        return Units.inches_to_meters(SystemSettings.kDriveWheelDiameterInches / 2.0);
    }

    @Override
    public double getWheelbaseRadiusMeters() {
        return Units.inches_to_meters(SystemSettings.kDriveEffectiveWheelbase / 2.0);
    }

    @Override
    public double getWheelbaseScrubFactor() {
        return 1.0;
    }

    @Override
    public double getLinearInertia() {
        return 45.35;
    }

    @Override
    public double getAngularInertia() {
        return 1.0;
    }

    @Override
    public double getAngularDrag() {
        return 0;
    }

}
