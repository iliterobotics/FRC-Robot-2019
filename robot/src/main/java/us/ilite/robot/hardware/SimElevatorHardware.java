package us.ilite.robot.hardware;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import com.team254.lib.geometry.Rotation2d;

import us.ilite.common.lib.RobotProfile;
import us.ilite.common.lib.control.DriveController;
import us.ilite.common.lib.odometry.Kinematics;
import us.ilite.common.lib.odometry.RobotStateEstimator;
import us.ilite.common.lib.util.Conversions;
import us.ilite.lib.drivers.Clock;
import us.ilite.robot.modules.DriveMessage;



public class SimElevatorHardware implements IDriveHardware {
    

    private final ILog mLogger = Logger.createLog(DriveHardware.class);

    // private final SimTalonEncoder mEncoderEstimator;

    public SimTalonEncoder mMasterSim = new SimTalonEncoder();
    public SimTalonEncoder mFollowerSim = new SimTalonEncoder();

    private Clock mClock;
    private double mLastTime = 0d;

    // public SimElevatorHardware


    public void set(DriveMessage pDriveMessage) {

    }

    public void configureMode(ControlMode pControlMode) {

    }

    public Rotation2d getHeading() {
        return null;
    }

    public double getLeftInches() {
        return null;
    }

    public double getRightInches() {
        return null;
    }

    public double getLeftVelInches() {
        return null;
    }

    public double getRightVelInches() {
        return null;
    }

    public int getLeftVelTicks() {
        return null;
    }

    public int getRightVelTicks() {
        return null;
    }

    public double getLeftCurrent() {

    }

    public double getRightCurrent() {

    }

    public double getLeftVoltage() {

    }

    public double getRightVoltage() {

    }

    public void init() {

    }

    public void zero() {

    }

    public boolean checkHardware() {

    }

}