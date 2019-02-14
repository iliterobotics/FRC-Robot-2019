package com.team254.frc2018;

import com.team254.lib.geometry.Pose2d;
import com.team254.lib.geometry.Rotation2d;
import com.team254.lib.geometry.Twist2d;
import com.team254.lib.util.InterpolatingDouble;
import com.team254.lib.util.InterpolatingTreeMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Port from Team 254's 2018 robot code (https://github.com/Team254/FRC-2018-Public) slightly modified to support a generic kinematics model.
 */
public class RobotState {

    private static final int kObservationBufferSize = 100;

    private Kinematics mKinematicModel;

    // FPGATimestamp -> RigidTransform2d or Rotation2d
    private InterpolatingTreeMap<InterpolatingDouble, Pose2d> field_to_vehicle_;
    private Twist2d vehicle_velocity_measured_;
    private Twist2d vehicle_displacement_measured_;
    private double distance_driven_;

    public RobotState(Kinematics pKinematicModel) {
        this.mKinematicModel = pKinematicModel;
        reset(0, new Pose2d());
    }

    /**
     * Resets the field to robot transform (robot's position on the field)
     */
    public synchronized void reset(double start_time, Pose2d initial_field_to_vehicle) {
        field_to_vehicle_ = new InterpolatingTreeMap<>(kObservationBufferSize);
        field_to_vehicle_.put(new InterpolatingDouble(start_time), initial_field_to_vehicle);
        vehicle_velocity_measured_ = Twist2d.identity();
        vehicle_displacement_measured_ = Twist2d.identity();
        distance_driven_ = 0.0;
    }

    public synchronized void resetDistanceDriven() {
        distance_driven_ = 0.0;
    }

    /**
     *
     * @param timestamp
     * @return Returns the robot's position on the field at a certain time. Linearly interpolates between stored robot positions
     * to fill in the gaps.
     */
    public synchronized Pose2d getFieldToVehicle(double timestamp) {
        return field_to_vehicle_.getInterpolated(new InterpolatingDouble(timestamp));
    }

    /**
     *
     * @return The latest observation from the pose buffer
     */
    public synchronized Map.Entry<InterpolatingDouble, Pose2d> getLatestFieldToVehicle() {
        return field_to_vehicle_.lastEntry();
    }

    /**
     *
     * @return The latest observation from the pose buffer, minus the timestamp.
     */
    public synchronized Pose2d getLatestFieldToVehiclePose() {
        return getLatestFieldToVehicle().getValue().getPose();
    }

    /**
     *
     * @param lookahead_time
     * @return The predicted position of the robot based on our stored predicted_velocity.
     */
    public synchronized Pose2d getPredictedFieldToVehicle(double lookahead_time) {
        // dx = v * t
        return getLatestFieldToVehicle().getValue()
                .transformBy(Pose2d.exp(vehicle_velocity_measured_.scaled(lookahead_time)));
    }

    public synchronized void addFieldToVehicleObservation(double timestamp, Pose2d observation) {
        field_to_vehicle_.put(new InterpolatingDouble(timestamp), observation);
    }

    public synchronized Pose2d
    addObservations(double timestamp, Twist2d measured_displacement,
                                             Twist2d measured_velocity) {
        Pose2d new_pose = mKinematicModel.integrateForwardKinematics(getLatestFieldToVehicle().getValue(), measured_displacement);
        addFieldToVehicleObservation(timestamp, new_pose);
        vehicle_displacement_measured_ = measured_displacement;
        vehicle_velocity_measured_ = measured_velocity;

        return new_pose;
    }

    public synchronized Twist2d generateOdometryFromSensors(double left_encoder_delta_distance, double
            right_encoder_delta_distance, Rotation2d current_gyro_angle) {
        final Pose2d last_measurement = getLatestFieldToVehicle().getValue();
        final Twist2d delta = mKinematicModel.forwardKinematics(last_measurement.getRotation(),
                left_encoder_delta_distance, right_encoder_delta_distance,
                current_gyro_angle);
        distance_driven_ += delta.dx;
        return delta;
    }

    public synchronized Twist2d generateOdometryFromSensors(double left_encoder_delta_distance, double
            right_encoder_delta_distance) {
        final Twist2d delta = mKinematicModel.forwardKinematics(left_encoder_delta_distance, right_encoder_delta_distance);
        distance_driven_ += delta.dx;
        return delta;
    }

    public List<Pose2d> getRecentPoses() {
        List<Pose2d> poseList = new ArrayList<>();
        for(Map.Entry<InterpolatingDouble, Pose2d> poseEntry : field_to_vehicle_.entrySet()) {
            poseList.add(poseEntry.getValue());
        }
        return poseList;
    }

    public synchronized double getDistanceDriven() {
        return distance_driven_;
    }

    public synchronized Twist2d getMeasuredVelocity() {
        return vehicle_velocity_measured_;
    }

    public synchronized Twist2d getMeasuredDisplacement() {
        return vehicle_displacement_measured_;
    }

}
