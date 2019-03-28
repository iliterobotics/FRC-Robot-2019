package us.ilite.common.lib.util;

import com.team254.lib.util.CheesyDriveGains;
import com.team254.lib.util.DriveSignal;
import com.team254.lib.util.Util;

/**
 * Helper class to implement "Cheesy Drive". "Cheesy Drive" simply means that the "turning" stick controls the curvature
 * of the robot's path rather than its rate of heading change. This helps make the robot more controllable at high
 * speeds. Also handles the robot's quick turn functionality - "quick turn" overrides constant-curvature turning for
 * turn-in-place maneuvers.
 */
public class CheesyDriveHelper {

    private final CheesyDriveGains mCheesyDriveGains;

    private double mOldWheel = 0.0;
    private double mQuickStopAccumulator = 0.0;
    private double mNegInertiaAccumulator = 0.0;

    public CheesyDriveHelper(CheesyDriveGains pCheesyDriveGains) {
        mCheesyDriveGains = pCheesyDriveGains;
    }

    public DriveSignal cheesyDrive(double throttle, double wheel, boolean isQuickTurn) {

        // Apply deadbands
        wheel = handleDeadband(wheel, mCheesyDriveGains.kWheelDeadband);
        throttle = handleDeadband(throttle, mCheesyDriveGains.kThrottleDeadband);

        double negInertia = wheel - mOldWheel;
        mOldWheel = wheel;

        double wheelNonLinearity;
        wheelNonLinearity = mCheesyDriveGains.kWheelNonLinearity;
        final double denominator = Math.sin(Math.PI / 2.0 * wheelNonLinearity);
        // Apply a sin function that's scaled to make it feel better.
        wheel = Math.sin(Math.PI / 2.0 * wheelNonLinearity * wheel) / denominator;
        wheel = Math.sin(Math.PI / 2.0 * wheelNonLinearity * wheel) / denominator;
        wheel = Math.sin(Math.PI / 2.0 * wheelNonLinearity * wheel) / denominator;

        double leftPwm, rightPwm, overPower;
        double sensitivity;

        double angularPower;
        double linearPower;

        // Negative inertia!
        double negInertiaScalar;
        if (wheel * negInertia > 0) {
            // If we are moving away from 0.0, aka, trying to get more wheel.
            negInertiaScalar = mCheesyDriveGains.kNegInertiaTurnScalar;
        } else {
            // Otherwise, we are attempting to go back to 0.0.
            if (Math.abs(wheel) > mCheesyDriveGains.kNegInertiaThreshold) {
                negInertiaScalar = mCheesyDriveGains.kNegInertiaFarScalar;
            } else {
                negInertiaScalar = mCheesyDriveGains.kNegInertiaCloseScalar;
            }
        }
        sensitivity = mCheesyDriveGains.kWheelSensitivity;

        double negInertiaPower = negInertia * negInertiaScalar;
        mNegInertiaAccumulator += negInertiaPower;

        wheel = wheel + mNegInertiaAccumulator;
        if (mNegInertiaAccumulator > 1) {
            mNegInertiaAccumulator -= 1;
        } else if (mNegInertiaAccumulator < -1) {
            mNegInertiaAccumulator += 1;
        } else {
            mNegInertiaAccumulator = 0;
        }
        linearPower = throttle;

        // Quickturn!
        if (isQuickTurn) {
            if (Math.abs(linearPower) < mCheesyDriveGains.kQuickStopDeadband) {
                double alpha = mCheesyDriveGains.kQuickStopWeight;
                mQuickStopAccumulator = (1 - alpha) * mQuickStopAccumulator
                        + alpha * Util.limit(wheel, 1.0) * mCheesyDriveGains.kQuickStopScalar;
            }
            overPower = 1.0;
            angularPower = wheel;
        } else {
            overPower = 0.0;
            angularPower = Math.abs(throttle) * wheel * sensitivity - mQuickStopAccumulator;
            if (mQuickStopAccumulator > 1) {
                mQuickStopAccumulator -= 1;
            } else if (mQuickStopAccumulator < -1) {
                mQuickStopAccumulator += 1;
            } else {
                mQuickStopAccumulator = 0.0;
            }
        }

        rightPwm = leftPwm = linearPower;
        leftPwm += angularPower;
        rightPwm -= angularPower;

        if (leftPwm > 1.0) {
            rightPwm -= overPower * (leftPwm - 1.0);
            leftPwm = 1.0;
        } else if (rightPwm > 1.0) {
            leftPwm -= overPower * (rightPwm - 1.0);
            rightPwm = 1.0;
        } else if (leftPwm < -1.0) {
            rightPwm += overPower * (-1.0 - leftPwm);
            leftPwm = -1.0;
        } else if (rightPwm < -1.0) {
            leftPwm += overPower * (-1.0 - rightPwm);
            rightPwm = -1.0;
        }
        return new DriveSignal(leftPwm, rightPwm);
    }

    public double handleDeadband(double val, double deadband) {
        return (Math.abs(val) > Math.abs(deadband)) ? val : 0.0;
    }

}
