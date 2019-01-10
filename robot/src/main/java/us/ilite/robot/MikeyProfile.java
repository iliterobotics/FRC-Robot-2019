package us.ilite.robot;

import us.ilite.common.lib.RobotProfile;
import us.ilite.common.config.SystemSettings;
import com.team254.lib.util.Units;

public class MikeyProfile implements RobotProfile {
    @Override
    public double getRightVoltPerAccel() {
        return 0.025850968660881886;
    }

    @Override
    public double getRightVoltPerSpeed() {
        return 0.28955111027686886;
    }

    @Override
    public double getRightFrictionVoltage() {
        return 0.4617679775968382;
    }

    @Override
    public double getLeftVoltPerAccel() {
        return 0.03311110204353067;
    }

    @Override
    public double getLeftVoltPerSpeed() {
        return 0.30325193440586623;
    }

    @Override
    public double getLeftFrictionVoltage() {
        return 0.4457170139555499;
    }

    @Override
    public double getWheelRadiusMeters() {
        return Units.inches_to_meters(SystemSettings.kDriveWheelDiameterInches / 2.0);
    }

    @Override
    public double getWheelbaseRadiusMeters() {
        return Units.inches_to_meters(24.5) / 2.0;
    }

    @Override
    public double getWheelbaseScrubFactor() {
        return 1.0;
    }

    @Override
    public double getLinearInertia() {
        return 27.2155;
    }

    @Override
    public double getAngularInertia() {
        return 1.0;
    }

    @Override
    public double getAngularDrag() {
        return 1.0;
    }
}
