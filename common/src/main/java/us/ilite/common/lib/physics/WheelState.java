package us.ilite.common.lib.physics;

import java.text.DecimalFormat;

// Can refer to velocity, acceleration, torque, voltage, etc., depending on context.
public class WheelState {
    public double left;
    public double right;

    public WheelState(double left, double right) {
        this.left = left;
        this.right = right;
    }

    public WheelState() {
    }

    public double get(boolean get_left) {
        return get_left ? left : right;
    }

    public void set(boolean set_left, double val) {
        if (set_left) {
            left = val;
        } else {
            right = val;
        }
    }

    @Override
    public String toString() {
        DecimalFormat fmt = new DecimalFormat("#0.000");
        return fmt.format(left) + ", " + fmt.format(right);
    }
}
