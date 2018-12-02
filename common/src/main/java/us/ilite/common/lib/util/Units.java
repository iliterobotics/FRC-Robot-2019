package us.ilite.common.lib.util;

import us.ilite.common.config.SystemSettings;

public class Units {
    public static double rpm_to_rads_per_sec(double rpm) {
        return rpm * 2.0 * Math.PI / 60.0;
    }

    public static double rads_per_sec_to_rpm(double rads_per_sec) {
        return rads_per_sec * 60.0 / (2.0 * Math.PI);
    }

    public static double inches_to_meters(double inches) {
        return inches * 0.0254;
    }

    public static double meters_to_inches(double meters) {
        return meters / 0.0254;
    }

    public static double feet_to_meters(double feet) {
        return inches_to_meters(feet * 12.0);
    }

    public static double meters_to_feet(double meters) {
        return meters_to_inches(meters) / 12.0;
    }

    public static double degrees_to_radians(double degrees) {
        return Math.toRadians(degrees);
    }

    public static double radians_to_degrees(double radians) {
        return Math.toDegrees(radians);
    }

    public static double tick_to_rotations(double ticks) {
        return ticks / SystemSettings.DRIVETRAIN_ENC_TICKS_PER_TURN;
    }

    public static double ticks_to_inches(double ticks) {
        return tick_to_rotations(ticks) * SystemSettings.DRIVETRAIN_WHEEL_CIRCUMFERENCE;
    }

    public static double vel_ticks_to_rpm(double ticks) {
        return tick_to_rotations(ticks) * 60000;
    }

    public static double vel_ticks_to_fps(double ticks) {
        return tick_to_rotations(ticks) * SystemSettings.DRIVETRAIN_WHEEL_CIRCUMFERENCE * (1.0 / 12.0) * 10.0;
    }

    public static double vel_ticks_to_rads(double ticks) {
        return tick_to_rotations(ticks) * 2 * Math.PI * 10.0;
    }

    public static double rads_to_ticks(double rads) {
        return rot_to_ticks(rads / 2.0 * Math.PI);
    }

    public static double fps_to_ticks(double fps) {
        return fps * 12 * (1 / SystemSettings.DRIVETRAIN_WHEEL_CIRCUMFERENCE) * SystemSettings.DRIVETRAIN_ENC_TICKS_PER_TURN * (1 / 1000) * (1 / 10);
    }

    public static double rot_to_ticks(double rotations) {
        return rotations * SystemSettings.DRIVETRAIN_ENC_TICKS_PER_TURN;
    }

    public static double inches_to_ticks(double inches) {
        return rot_to_ticks(inches / SystemSettings.DRIVETRAIN_WHEEL_CIRCUMFERENCE);
    }

}
