package profiles;

import lib.util.Units;

/**
 * The dynamics information for Team 254's 2018 robot, Lockdown.
 */
public class LockdownProfile implements RobotProfile {

    @Override
    public double getLeftVoltPerAccel() {
        return 0.012;
    }

    @Override
    public double getLeftVoltPerSpeed() {
        return 0.135;
    }

    @Override
    public double getLeftFrictionVoltage() {
        return 1.055;
    }

    @Override
    public double getRightVoltPerAccel() {
        return 0.012;
    }

    @Override
    public double getRightVoltPerSpeed() {
        return 0.135;
    }

    @Override
    public double getRightFrictionVoltage() {
        return 1.055;
    }

    @Override
    public double getWheelRadiusMeters() {
        return Units.inches_to_meters(3.92820959548 * 0.99) / 2.0;
    }

    @Override
    public double getWheelbaseRadiusMeters() {
        return Units.inches_to_meters(25.54 / 2.0);
    }

    @Override
    public double getWheelbaseScrubFactor() {
        return 1.0;
    }

    @Override
    public double getLinearInertia() {
        return 60.0;
    }

    @Override
    public double getAngularInertia() {
        return 10.0;
    }

    @Override
    public double getAngularDrag() {
        return 12.0;
    }

}
