package us.ilite.robot;

import us.ilite.common.lib.RobotProfile;
import us.ilite.common.config.SystemSettings;
import com.team254.lib.util.Units;

public class StrongholdProfile implements RobotProfile {
    @Override
    public double getLeftVoltPerAccel() {
        return 0.006004249716616279;
    }

    @Override
    public double getLeftVoltPerSpeed() {
        return 0.1532122569317568;
    }

    @Override
    public double getLeftFrictionVoltage() {
        return 0.8889335719734218;
    }

    @Override
    public double getRightVoltPerAccel() {
        return 0.007596397142391264;
    }

    @Override
    public double getRightVoltPerSpeed() {
        return 0.1644602870912295;
    }

    @Override
    public double getRightFrictionVoltage() {
        return 0.8655779296499259;
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
        return 1;
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
