package lib.trajectory;

import lib.geometry.Pose2d;
import lib.geometry.Rotation2d;

import java.util.ArrayList;
import java.util.List;

public class WaypointUtil {

    private static final Pose2d flip = Pose2d.fromRotation(new Rotation2d(-1, 0, false));

    public static List<Pose2d> flipWaypoints(List<Pose2d> pWaypoints) {
        List<Pose2d> waypoints_maybe_flipped = new ArrayList<>(pWaypoints.size());
        for (int i = 0; i < pWaypoints.size(); ++i) {
            waypoints_maybe_flipped.add(pWaypoints.get(i).transformBy(flip));
        }
        return waypoints_maybe_flipped;
    }

}
