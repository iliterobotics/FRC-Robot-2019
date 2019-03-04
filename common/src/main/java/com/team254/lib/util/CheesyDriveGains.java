package com.team254.lib.util;

public class CheesyDriveGains {

    public double kThrottleDeadband = 0.02;
    public double kWheelDeadband = 0.02;

    // These factor determine how fast the wheel traverses the "non linear" sine curve.
    public double kWheelNonLinearity = 0.5;


    public double kNegInertiaThreshold = 0.65;
    public double kNegInertiaTurnScalar = 3.5;
    public double kNegInertiaCloseScalar = 4.0;
    public double kNegInertiaFarScalar = 5.0;

    public double kWheelSensitivity = 0.85;

    public double kQuickStopDeadband = 0.5;
    public double kQuickStopWeight = 0.1;
    public double kQuickStopScalar = 5.0;

}
