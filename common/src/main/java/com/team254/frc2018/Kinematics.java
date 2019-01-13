package com.team254.frc2018;

import us.ilite.common.lib.RobotProfile;
import com.team254.lib.geometry.Pose2d;
import com.team254.lib.geometry.Rotation2d;
import com.team254.lib.geometry.Twist2d;
import com.team254.lib.util.Units;

/**
 * Provides forward and inverse kinematics equations for the robot modeling the wheelbase as a differential drive (with
 * a corrective factor to account for skidding). 
 * Port from Team 254's 2018 robot code (https://github.com/Team254/FRC-2018-Public) slightly modified to support a generic set of robot parameters.
 */

public class Kinematics {

    private static final double kEpsilon = 1E-9;

    public final double kDriveWheelTrackWidthInches;
    public final double kTrackScrubFactor;

    public Kinematics(RobotProfile pRobotProfile) {
        kDriveWheelTrackWidthInches = Units.meters_to_inches(pRobotProfile.getWheelbaseRadiusMeters() * 2);
        kTrackScrubFactor = pRobotProfile.getWheelbaseScrubFactor();
    }

    /**
     * Forward kinematics using only encoders, rotation is implicit (less accurate than below, but useful for predicting
     * motion)
     */
    public Twist2d forwardKinematics(double left_wheel_delta, double right_wheel_delta) {
        double delta_rotation = (right_wheel_delta - left_wheel_delta) / (kDriveWheelTrackWidthInches * kTrackScrubFactor);
        return forwardKinematics(left_wheel_delta, right_wheel_delta, delta_rotation);
    }

    public Twist2d forwardKinematics(double left_wheel_delta, double right_wheel_delta, double delta_rotation_rads) {
        final double dx = (left_wheel_delta + right_wheel_delta) / 2.0;
        return new Twist2d(dx, 0.0, delta_rotation_rads);
    }

    public Twist2d forwardKinematics(Rotation2d prev_heading, double left_wheel_delta, double right_wheel_delta,
                                            Rotation2d current_heading) {
        final double dx = (left_wheel_delta + right_wheel_delta) / 2.0;
        final double dy = 0.0;
        return new Twist2d(dx, dy, prev_heading.inverse().rotateBy(current_heading).getRadians());
    }

    /**
     * For convenience, integrate forward kinematics with a Twist2d and previous rotation.
     */
    public Pose2d integrateForwardKinematics(Pose2d current_pose,
                                                    Twist2d forward_kinematics) {
        return current_pose.transformBy(Pose2d.exp(forward_kinematics));
    }

}
