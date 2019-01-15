package us.ilite.display.simulation;

import java.util.Arrays;
import java.util.List;

import com.flybotix.hfr.util.log.ELevel;
import com.flybotix.hfr.util.log.Logger;
import com.team254.lib.geometry.Pose2d;
import com.team254.lib.geometry.Pose2dWithCurvature;
import com.team254.lib.geometry.Rotation2d;
import com.team254.lib.trajectory.Trajectory;
import com.team254.lib.trajectory.timing.CentripetalAccelerationConstraint;
import com.team254.lib.trajectory.timing.TimedState;
import com.team254.lib.trajectory.timing.TimingConstraint;
import com.team254.lib.util.ReflectingCSVWriter;

import us.ilite.common.lib.RobotProfile;
import us.ilite.common.lib.control.DriveController;
import com.team254.frc2018.planners.DriveMotionPlanner;
import us.ilite.common.lib.odometry.RobotStateEstimator;
import us.ilite.common.lib.trajectory.TrajectoryGenerator;
import us.ilite.lib.drivers.Clock;
import us.ilite.robot.Data;
import us.ilite.robot.StrongholdProfile;
import us.ilite.robot.auto.paths.middle.MiddleToMiddleCargoToSideRocket;
import us.ilite.robot.modules.Drive;

public class TrackingSimulation {

    private final double kDt;

    // in / s
    private final double kMaxLinearVel = 130.0; // 10 ft/s -> 120 in / s
    private final double kMaxLinearAccel = 130.0;
    private final double kMaxCentripetalAccel = /*100.0*/70.0;
    private final double kMaxVoltage = 9.0;

    private final List<TimingConstraint<Pose2dWithCurvature>> kTrajectoryConstraints = Arrays.asList(new CentripetalAccelerationConstraint(kMaxCentripetalAccel));

    private ReflectingCSVWriter<Pose2d> csvPoseWriter;
    private ReflectingCSVWriter<DriveMotionPlanner> csvDrivePlanner;

    private final Data mData;
    private final Clock mClock;
    private final DriveController mDriveController;
    private final TrajectoryGenerator mTrajectoryGenerator;
    private final Drive mDrive;
    private final DriveSimulation mDriveSimulation;

    public TrackingSimulation(double pDt) {
        this(new StrongholdProfile(), pDt);
    }

    public TrackingSimulation(RobotProfile pRobotProfile, double pDt) {
        kDt = pDt;
        mData = new Data();
        mClock = new Clock().simulated();
        mDriveController = new DriveController(new StrongholdProfile());
        mTrajectoryGenerator = new TrajectoryGenerator(mDriveController);
        mDrive = new Drive(mData, mDriveController, mClock, true);
        csvPoseWriter = new ReflectingCSVWriter<>("tracking.csv", Pose2d.class);
        csvDrivePlanner = new ReflectingCSVWriter<>("trajectory.csv", DriveMotionPlanner.class);
        mDriveSimulation = new DriveSimulation(mDrive, csvPoseWriter, csvDrivePlanner, kDt);
    }

    public void simulate() {

        Logger.setLevel(ELevel.DEBUG);

        double timeDriven = 0.0;

        // timeDriven += mDriveSimulation.driveTrajectory(generate(NearScaleAuto.kToScalePath), true);
        // timeDriven += mDriveSimulation.driveTrajectory(generate(NearScaleAuto.kAtScale.getRotation(), NearScaleAuto.kTurnFromScaleToFirstCube.getRotation()));
        // timeDriven += mDriveSimulation.driveTrajectory(generate(NearScaleAuto.kScaleToFirstCubePath), false);
        // timeDriven += mDriveSimulation.driveTrajectory(generate(NearScaleAuto.kScaleToFirstCube.getRotation(), NearScaleAuto.kTurnFromFirstCubeToScale.getRotation()));
        // timeDriven += mDriveSimulation.driveTrajectory(generate(NearScaleAuto.kFirstCubeToScalePath), false);

        timeDriven += mDriveSimulation.driveTrajectory(generate(MiddleToMiddleCargoToSideRocket.kStartToMiddleLeftHatchPath), true);

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
