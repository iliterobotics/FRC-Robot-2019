package us.ilite.display.simulation;

import control.DriveController;
import control.DriveMotionPlanner;
import odometry.RobotStateEstimator;
import paths.TrajectoryGenerator;
import paths.autos.NearScaleAuto;
import profiles.LockdownProfile;
import profiles.RobotProfile;
import us.ilite.common.lib.geometry.Pose2d;
import us.ilite.common.lib.geometry.Pose2dWithCurvature;
import us.ilite.common.lib.geometry.Rotation2d;
import us.ilite.common.lib.trajectory.Trajectory;
import us.ilite.common.lib.trajectory.timing.CentripetalAccelerationConstraint;
import us.ilite.common.lib.trajectory.timing.TimedState;
import us.ilite.common.lib.trajectory.timing.TimingConstraint;
import us.ilite.common.lib.util.ReflectingCSVWriter;

import java.util.Arrays;
import java.util.List;

public class TrackingSimulation {

    private final double kDt;

    private final DriveController mDriveController;
    private final TrajectoryGenerator mTrajectoryGenerator;

    // in / s
    private final double kMaxLinearVel = 130.0; // 10 ft/s -> 120 in / s
    private final double kMaxLinearAccel = 130.0;
    private final double kMaxCentripetalAccel = /*100.0*/70.0;
    private final double kMaxVoltage = 9.0;

    private final List<TimingConstraint<Pose2dWithCurvature>> kTrajectoryConstraints = Arrays.asList(new CentripetalAccelerationConstraint(kMaxCentripetalAccel));

    private ReflectingCSVWriter<Pose2d> csvPoseWriter;
    private ReflectingCSVWriter<DriveMotionPlanner> csvDrivePlanner;

    private DriveSimulation mDriveSimulation;

    public TrackingSimulation(double pDt) {
        this(new LockdownProfile(), pDt);
    }

    public TrackingSimulation(RobotProfile pRobotProfile, double pDt) {
        kDt = pDt;
        mDriveController = new DriveController(new LockdownProfile(), 0.01);
        mTrajectoryGenerator = new TrajectoryGenerator(mDriveController);
        csvPoseWriter = new ReflectingCSVWriter<>("tracking.csv", Pose2d.class);
        csvDrivePlanner = new ReflectingCSVWriter<>("trajectory.csv", DriveMotionPlanner.class);
        mDriveSimulation = new DriveSimulation(mDriveController, csvPoseWriter, csvDrivePlanner, kDt);
    }

    public void simulate() {

        double timeDriven = 0.0;

        timeDriven += mDriveSimulation.driveTrajectory(generate(NearScaleAuto.kToScalePath), true);
        timeDriven += mDriveSimulation.driveTrajectory(generate(NearScaleAuto.kAtScale.getRotation(), NearScaleAuto.kTurnFromScaleToFirstCube.getRotation()));
        timeDriven += mDriveSimulation.driveTrajectory(generate(NearScaleAuto.kScaleToFirstCubePath), false);
        timeDriven += mDriveSimulation.driveTrajectory(generate(NearScaleAuto.kScaleToFirstCube.getRotation(), NearScaleAuto.kTurnFromFirstCubeToScale.getRotation()));
        timeDriven += mDriveSimulation.driveTrajectory(generate(NearScaleAuto.kFirstCubeToScalePath), false);

        System.out.println("Time Driven:" + timeDriven);

    }

    public Trajectory<TimedState<Pose2dWithCurvature>> generate(List<Pose2d> waypoints) {
        return generate(false, waypoints);
    }

    public Trajectory<TimedState<Pose2dWithCurvature>> generate(boolean reversed, List<Pose2d> waypoints) {
        return mTrajectoryGenerator.generateTrajectory(reversed, waypoints, kTrajectoryConstraints, kMaxLinearVel, kMaxLinearAccel, kMaxVoltage);
    }

    public Trajectory<TimedState<Rotation2d>> generate(double initialHeading, double finalHeading ) {
        return mTrajectoryGenerator.generateTurnInPlaceTrajectory(initialHeading, finalHeading, kTrajectoryConstraints,0.0, kMaxLinearVel, kMaxLinearAccel, kMaxVoltage);
    }

    public Trajectory<TimedState<Rotation2d>> generate(Rotation2d initialHeading, Rotation2d finalHeading ) {
        return mTrajectoryGenerator.generateTurnInPlaceTrajectory(initialHeading, finalHeading, kTrajectoryConstraints,0.0, kMaxLinearVel, kMaxLinearAccel, kMaxVoltage);
    }

    public DriveSimulation getDriveSimulation() {
        return mDriveSimulation;
    }

    public RobotStateEstimator getRobotStateEstimator() {
        return mDriveController.getRobotStateEstimator();
    }

}
