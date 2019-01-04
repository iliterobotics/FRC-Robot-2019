package us.ilite.common.lib;

/**
 * Stores dynamics information about our robot.
 */
public interface RobotProfile {

    // Volts, meters
    double getLeftVoltPerAccel();
    double getLeftVoltPerSpeed();
    double getLeftFrictionVoltage();

    double getRightVoltPerAccel();
    double getRightVoltPerSpeed();
    double getRightFrictionVoltage();

    // Meters
    double getWheelRadiusMeters();
    double getWheelbaseRadiusMeters();
    // Multiplied by wheelbase to obtain effective wheelbase
    double getWheelbaseScrubFactor();

    // kg * m^2
    double getLinearInertia();
    double getAngularInertia();
    // Allows us to model motor load as wheel w/ mass of robot
    default double getCylindricalMoi() {
        return 0.5 * getLinearInertia() * (getWheelRadiusMeters() * getWheelRadiusMeters());
    }

    // friction_force / w
    double getAngularDrag();
    
}
