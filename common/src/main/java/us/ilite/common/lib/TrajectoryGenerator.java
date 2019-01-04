package us.ilite.common.lib;

import com.team254.lib.geometry.State;
import com.team254.lib.trajectory.TrajectoryIterator;
import com.team254.lib.util.Util;
import us.ilite.common.lib.control.DriveController;
import us.ilite.common.lib.control.DriveMotionPlanner;
import com.team254.lib.geometry.Pose2d;
import com.team254.lib.geometry.Pose2dWithCurvature;
import com.team254.lib.geometry.Rotation2d;
import com.team254.lib.trajectory.DistanceView;
import com.team254.lib.trajectory.Trajectory;
import com.team254.lib.trajectory.TrajectoryUtil;
import com.team254.lib.trajectory.timing.DifferentialDriveDynamicsConstraint;
import com.team254.lib.trajectory.timing.TimedState;
import com.team254.lib.trajectory.timing.TimingConstraint;
import com.team254.lib.trajectory.timing.TimingUtil;
import com.team254.lib.util.Units;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TrajectoryGenerator {

    private static final Pose2d flip = Pose2d.fromRotation(new Rotation2d(-1, 0, false));

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

        trajectory = (reversed) ? flip(trajectory) : trajectory;

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

        Trajectory<TimedState<Rotation2d>> timedRotationDeltaTrajectory = distanceToRotation(wheelTrajectory,
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

    public static Trajectory<Pose2dWithCurvature> flip(final Trajectory<Pose2dWithCurvature> trajectory) {
        List<Pose2dWithCurvature> flipped = new ArrayList<>(trajectory.length());
        for (int i = 0; i < trajectory.length(); ++i) {
            flipped.add(new Pose2dWithCurvature(trajectory.getState(i).getPose().transformBy(flip), -trajectory
                    .getState(i).getCurvature(), trajectory.getState(i).getDCurvatureDs()));
        }
        return new Trajectory<>(flipped);
    }

    public static Trajectory<TimedState<Rotation2d>> distanceToRotation(Trajectory<TimedState<Pose2dWithCurvature>> distanceTrajectory,
                                                                        Rotation2d initial_heading,
                                                                        double wheelbase_radius_inches) {
        List<TimedState<Rotation2d>> timedRotationStates = new ArrayList<>();

        for(int i = 0; i < distanceTrajectory.length(); i++) {
            TimedState<Pose2dWithCurvature> timedState = distanceTrajectory.getState(i);
            Rotation2d delta_heading = Rotation2d.fromRadians(timedState.state().getTranslation().x() / wheelbase_radius_inches);
            Rotation2d absolute_heading = initial_heading.rotateBy(delta_heading);
            TimedState<Rotation2d> timedRotationState = new TimedState<>(absolute_heading,
                    timedState.t(),
                    timedState.velocity() / wheelbase_radius_inches,
                    timedState.acceleration() / wheelbase_radius_inches);
            timedRotationStates.add(timedRotationState);
        }

        return new Trajectory<>(timedRotationStates);
    }

    public static <S extends State<S>> boolean isReversed(TrajectoryIterator<TimedState<S>> trajectoryIterator) {
        boolean reversed = false;
        for (int i = 0; i < trajectoryIterator.trajectory().length(); ++i) {
            if (trajectoryIterator.trajectory().getState(i).velocity() > Util.kEpsilon) {
                reversed = false;
                break;
            } else if (trajectoryIterator.trajectory().getState(i).velocity() < -Util.kEpsilon) {
                reversed = true;
                break;
            }
        }
        return reversed;
    }

}
