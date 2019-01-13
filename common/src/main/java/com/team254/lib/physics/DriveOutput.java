package com.team254.lib.physics;

/**
 * Contains all the information needed to control the drivebase.
 * Port from Team 254's 2018 robot code (https://github.com/Team254/FRC-2018-Public), 
 * with some extra utility methods.
 */
public class DriveOutput {
    public DriveOutput() {
    }

    public DriveOutput(double left_velocity, double right_velocity, double left_accel, double right_accel,
                       double left_feedforward_voltage, double
                          right_feedforward_voltage) {
        this.left_velocity = left_velocity;
        this.right_velocity = right_velocity;
        this.left_accel = left_accel;
        this.right_accel = right_accel;
        this.left_feedforward_voltage = left_feedforward_voltage;
        this.right_feedforward_voltage = right_feedforward_voltage;
    }

    public double left_velocity;  // rad/s
    public double right_velocity;  // rad/s

    public double left_accel;  // rad/s^2
    public double right_accel;  // rad/s^2

    public double left_feedforward_voltage;
    public double right_feedforward_voltage;

    public void flip() {
        double tmp_left_velocity = left_velocity;
        left_velocity = -right_velocity;
        right_velocity = -tmp_left_velocity;

        double tmp_left_accel = left_accel;
        left_accel = -right_accel;
        right_accel = -tmp_left_accel;

        double tmp_left_feedforward = left_feedforward_voltage;
        left_feedforward_voltage = -right_feedforward_voltage;
        right_feedforward_voltage = -tmp_left_feedforward;
    }

    public String toString() {
        return String.format("Left Vel: %s\tRight Vel: %s\n" +
                                 "Left Accel: %s\tRight Accel: %s\n" +
                                 "Left Voltage: %s\tRight Voltage: %s\n",
                                 left_velocity, right_velocity, left_accel, right_accel, left_feedforward_voltage, right_feedforward_voltage);
    }

    public DriveOutput rads_to_inches(double pWheelRadius) {
        return new DriveOutput(left_velocity * pWheelRadius, right_velocity * pWheelRadius,
                left_accel * pWheelRadius, right_accel * pWheelRadius, left_feedforward_voltage, right_feedforward_voltage);
    }

}
