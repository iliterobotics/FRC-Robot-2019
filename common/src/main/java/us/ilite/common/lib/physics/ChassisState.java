package us.ilite.common.lib.physics;

import us.ilite.common.lib.geometry.Twist2d;

import java.text.DecimalFormat;

// Can refer to velocity or acceleration depending on context.
public class ChassisState {
    public double linear;
    public double angular;

    public ChassisState(double linear, double angular) {
        this.linear = linear;
        this.angular = angular;
    }

    public ChassisState() {
    }

    public Twist2d toTwist() {
        return new Twist2d(linear, 0.0, angular);
    }

    @Override
    public String toString() {
        DecimalFormat fmt = new DecimalFormat("#0.000");
        return fmt.format(linear) + ", " + fmt.format(angular);
    }
}
