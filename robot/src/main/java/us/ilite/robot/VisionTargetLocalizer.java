package us.ilite.robot;

import com.team254.lib.geometry.Pose2d;
import com.team254.lib.geometry.Rotation2d;
import com.team254.lib.geometry.Translation2d;
import com.team254.lib.util.Util;
import us.ilite.robot.auto.paths.VisionTargetLocations;

import java.util.HashMap;
import java.util.Map;

public class VisionTargetLocalizer {

    private Map<VisionTargetLocations, Double> mTargetDistances = new HashMap<>();

    private final Translation2d vehicle_to_camera;

    public VisionTargetLocalizer(Translation2d vehicle_to_camera) {
        this.vehicle_to_camera = vehicle_to_camera;
    }

    public final Translation2d correctForCameraOffset(Translation2d target_to_camera) {
        return target_to_camera.translateBy(vehicle_to_camera.inverse());
    }

    /**
     *
     * @param field_to_vehicle The pose of the vehicle relative to the field.
     * @param target_to_camera The translation of the vehicle relative to the target.
     * @return A pose corrected based on the relative location of the nearest vision target.
     */
    public Pose2d getCorrectedPose(Pose2d field_to_vehicle, Translation2d target_to_camera) {

        Translation2d target_to_vehicle = correctForCameraOffset(target_to_camera);

        // Update distances
        for(VisionTargetLocations v : VisionTargetLocations.values()) {
            mTargetDistances.put(v, v.kLocation.distance(field_to_vehicle));
        }

        Pose2d field_to_target = Util.getEntryWithSmallestValue(mTargetDistances).getKey().kLocation;
        Pose2d corrected_field_to_vehicle = field_to_target.transformBy(Pose2d.fromTranslation(target_to_vehicle));

        return corrected_field_to_vehicle;

    }

}
