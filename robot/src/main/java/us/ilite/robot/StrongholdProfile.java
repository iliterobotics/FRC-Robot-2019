package us.ilite.robot;

import us.ilite.common.lib.RobotProfile;
import us.ilite.common.config.SystemSettings;
import com.team254.lib.util.Units;

/**
 * These values were obtained by running the "CharacterizeDrive" command with the robot on blocks.
 *
 * Wheelbase and wheel radius were obtained theoretically (from physical measurements with a tape measure). This shouldn't
 * affect performance, as this robot has corner omnis and thus does not experience wheel scrub.
 *
 * Linear and angular inertia terms are theoretically calculated (or not calculated at all...)
 */
public class StrongholdProfile implements RobotProfile {
    @Override
    public double getLeftVoltPerAccel() {
        return 0.020548508346599333;
    }

    @Override
    public double getLeftVoltPerSpeed() {
        return 0.393530524546484;
    }

    @Override
    public double getLeftFrictionVoltage() {
        return 1.099701306138158;
    }

    @Override
    public double getRightVoltPerAccel() {
        return 0.018778029411794406;
    }

    @Override
    public double getRightVoltPerSpeed() {
        return 0.37658382623100706;
    }

    @Override
    public double getRightFrictionVoltage() {
        return 1.0488696460140612;
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
        return 54.4311;
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
