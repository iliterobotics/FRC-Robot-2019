package us.ilite.common.lib.trajectory;

import com.team254.lib.geometry.Pose2dWithCurvature;
import com.team254.lib.trajectory.timing.TimingConstraint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple container class for commonly used trajectory constraints.
 */
public class TrajectoryConstraints {

    private final List<TimingConstraint<Pose2dWithCurvature>> mTimingConstraints;
    private final double mMaximumVelocity;
    private final double mMaximumAcceleration;
    private final double mMaximumVoltage;

    public TrajectoryConstraints(double pMaximumVelocity, double pMaximumAcceleration, double pMaximumVoltage, TimingConstraint<Pose2dWithCurvature> ... pTimingConstraints) {
        mMaximumVelocity = pMaximumVelocity;
        mMaximumAcceleration = pMaximumAcceleration;
        mMaximumVoltage = pMaximumVoltage;
        mTimingConstraints = new ArrayList<>();
        mTimingConstraints.addAll(Arrays.asList(pTimingConstraints));
    }

    public TrajectoryConstraints(TrajectoryConstraints pConstraints) {
        mMaximumVelocity = pConstraints.getMaximumVelocity();
        mMaximumAcceleration = pConstraints.getMaximumAcceleration();
        mMaximumVoltage = pConstraints.getMaximumVoltage();
        mTimingConstraints = pConstraints.getTimingConstraints();
    }

    public List<TimingConstraint<Pose2dWithCurvature>> getTimingConstraints() {
        return mTimingConstraints;
    }

    public double getMaximumVelocity() {
        return mMaximumVelocity;
    }

    public double getMaximumAcceleration() {
        return mMaximumAcceleration;
    }

    public double getMaximumVoltage() {
        return mMaximumVoltage;
    }

}
