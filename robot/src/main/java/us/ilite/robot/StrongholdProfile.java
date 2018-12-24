package us.ilite.robot;

import profiles.RobotProfile;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.lib.util.Units;

public class StrongholdProfile implements RobotProfile {
    @Override
    public double getLeftVoltPerAccel() {
        return 0.005948041266447381;
    }

    @Override
    public double getLeftVoltPerSpeed() {
        return 0.20978019726529204;
    }

    @Override
    public double getLeftFrictionVoltage() {
        return 0.629373421839725;
    }

    @Override
    public double getRightVoltPerAccel() {
        return 0.0040906524377900015;
    }

    @Override
    public double getRightVoltPerSpeed() {
        return 0.206559880661648;
    }

    @Override
    public double getRightFrictionVoltage() {
        return 0.7315794648276989;
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
        return 0;
    }

    @Override
    public double getLinearInertia() {
        return 20;
    }

    @Override
    public double getAngularInertia() {
        return 1;
    }

    @Override
    public double getAngularDrag() {
        return 0;
    }
}
