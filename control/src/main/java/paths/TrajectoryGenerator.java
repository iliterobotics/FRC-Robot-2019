package paths;

import control.DriveController;
import control.DriveMotionPlanner;
import us.ilite.common.lib.geometry.Pose2d;
import us.ilite.common.lib.geometry.Pose2dWithCurvature;
import us.ilite.common.lib.geometry.Rotation2d;
import us.ilite.common.lib.trajectory.DistanceView;
import us.ilite.common.lib.trajectory.Trajectory;
import us.ilite.common.lib.trajectory.TrajectoryUtil;
import us.ilite.common.lib.trajectory.timing.DifferentialDriveDynamicsConstraint;
import us.ilite.common.lib.trajectory.timing.TimedState;
import us.ilite.common.lib.trajectory.timing.TimingConstraint;
import us.ilite.common.lib.trajectory.timing.TimingUtil;
import us.ilite.common.lib.util.Units;
import profiles.RobotProfile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TrajectoryGenerator {

    // Maximum delta between each trajectory point
    private static final double kMaxDx = 2.0;
    private static final double kMaxDy = 0.25;
    private static final double kMaxDTheta = Math.toRadians(5.0);

    private DriveMotionPlanner mDriveMotionPlanner;
    private RobotProfile mRobotProfile;

    public TrajectoryGenerator(DriveController pDriveController) {
        mDriveMotionPlanner = pDriveController.getDriveMotionPlanner();
        mRobotProfile = pDriveController.getRobotProfile();
    }

    public Trajectory<TimedState<Pose2dWithCurvature>> generateTrajectory(
            boolean reversed,
            final List<Pose2d> waypoints,
            final List<TimingConstraint<Pose2dWithCurvature>> constraints,
            double max_vel,  // inches/s
            double max_accel,  // inches/s^2
            double max_voltage) {
        return generateTrajectory(reversed, waypoints, constraints, 0.0, 0.0, max_vel, max_accel, max_voltage);
    }

    public Trajectory<TimedState<Pose2dWithCurvature>> generateTrajectory(
            boolean reversed,
            final List<Pose2d> waypoints,
            final List<TimingConstraint<Pose2dWithCurvature>> constraints,
            double start_vel,
            double end_vel,
            double max_vel,  // inches/s
            double max_accel,  // inches/s^2
            double max_voltage) {

//        List<Pose2d> waypoints_maybe_flipped = (reversed) ? WaypointUtil.flipWaypoints(waypoints) : waypoints;
        // We'll assume that any paths passed to us are pointing in the correct direction (the direction the robot is actually moving) already.
        // In other words, we will consider the heading passed to us to be the heading w.r.t the BACK of the robot, NOT the front.
        List<Pose2d> waypoints_maybe_flipped = waypoints;


        // Create a trajectory from splines.
        Trajectory<Pose2dWithCurvature> trajectory = TrajectoryUtil.trajectoryFromSplineWaypoints(
                waypoints_maybe_flipped, kMaxDx, kMaxDy, kMaxDTheta);

        trajectory = (reversed) ? TrajectoryUtil.flip(trajectory) : trajectory;

        // Create the constraint that the robot must be able to traverse the trajectory without ever applying more
        // than the specified voltage.
        final DifferentialDriveDynamicsConstraint<Pose2dWithCurvature> drive_constraints = new
                DifferentialDriveDynamicsConstraint<>(mDriveMotionPlanner.getDriveModel(), max_voltage);
        List<TimingConstraint<Pose2dWithCurvature>> all_constraints = new ArrayList<>();
        all_constraints.add(drive_constraints);
        if (constraints != null) {
            all_constraints.addAll(constraints);
        }

        // Generate the timed trajectory.
        Trajectory<TimedState<Pose2dWithCurvature>> timed_trajectory = TimingUtil.timeParameterizeTrajectory
                (reversed, new
                        DistanceView<>(trajectory), kMaxDx, all_constraints, start_vel, end_vel, max_vel, max_accel);
        return timed_trajectory;
    }

    /**
     * Generates rotation trajectories by calculating wheel distances needed to achieve angle, then converting the generated trajectories into rotation states.
     * @param initial_heading
     * @param final_heading
     * @param constraints
     * @param end_vel
     * @param max_vel
     * @param max_accel
     * @param max_voltage
     * @return A rotation trajectory for the robot to follow while turning in place.
     */
    public Trajectory<TimedState<Rotation2d>> generateTurnInPlaceTrajectory(Rotation2d initial_heading,
                                                                            Rotation2d final_heading,
                                                                            final List<TimingConstraint<Pose2dWithCurvature>> constraints,
                                                                            double end_vel,
                                                                            double max_vel,  // inches/s
                                                                            double max_accel,  // inches/s^2
                                                                            double max_voltage) {

        Rotation2d rotation_delta = initial_heading.inverse().rotateBy(final_heading);

        // Find distance necessary to move wheels to achieve change in heading
        double distance = rotation_delta.getRadians() * Units.meters_to_inches(mRobotProfile.getWheelbaseRadiusMeters());
        List<Pose2d> wheelTravel = Arrays.asList(new Pose2d(0.0, 0.0, new Rotation2d()),
                new Pose2d(distance, 0.0, new Rotation2d()));

        // Create the constraint that the robot must be able to traverse the trajectory without ever applying more
        // than the specified voltage.
        final DifferentialDriveDynamicsConstraint<Pose2dWithCurvature> drive_constraints = new
                DifferentialDriveDynamicsConstraint<>(mDriveMotionPlanner.getDriveModel(), max_voltage);
        List<TimingConstraint<Pose2dWithCurvature>> all_constraints = new ArrayList<>();
        all_constraints.add(drive_constraints);
        if (constraints != null) {
            all_constraints.addAll(constraints);
        }

        Trajectory<TimedState<Pose2dWithCurvature>> wheelTrajectory = generateTrajectory(false, wheelTravel, all_constraints, 0.0, end_vel, max_vel, max_accel, max_voltage);

        Trajectory<TimedState<Rotation2d>> timedRotationDeltaTrajectory = TrajectoryUtil.distanceToRotation(wheelTrajectory,
                initial_heading,
                Units.meters_to_inches(mRobotProfile.getWheelbaseRadiusMeters()));

        return timedRotationDeltaTrajectory;
    }

    /**
     * For convenience, allow headings to be entered in degrees.
     * @param pInitialHeadingDegrees
     * @param pFinalHeadingDegrees
     * @param pTrajectoryConstraints
     * @param pEndVel
     * @param pMaxVel
     * @param pMaxAccel
     * @param pMaxVoltage
     * @return
     */
    public Trajectory<TimedState<Rotation2d>> generateTurnInPlaceTrajectory(double pInitialHeadingDegrees, double pFinalHeadingDegrees,
                                                                            List<TimingConstraint<Pose2dWithCurvature>> pTrajectoryConstraints,
                                                                            double pEndVel, double pMaxVel, double pMaxAccel, double pMaxVoltage) {
        return generateTurnInPlaceTrajectory(Rotation2d.fromDegrees(pInitialHeadingDegrees), Rotation2d.fromDegrees(pFinalHeadingDegrees),
                pTrajectoryConstraints, pEndVel, pMaxVel, pMaxAccel, pMaxVoltage);
    }

}
