package us.ilite.robot.hardware;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import com.team254.lib.geometry.Rotation2d;

import us.ilite.common.lib.RobotProfile;
import us.ilite.common.lib.control.DriveController;
import us.ilite.common.lib.odometry.RobotStateEstimator;
import us.ilite.common.lib.util.Conversions;
import us.ilite.lib.drivers.Clock;
import us.ilite.robot.modules.DriveMessage;
import us.ilite.robot.modules.EElevatorPosition;



public class SimElevatorHardware implements IElevatorHardware {
    
    private SimTalonEncoder mEncoderEstimator;

    public SimTalonEncoder mMasterSim = new SimTalonEncoder();

    private Clock mClock;
    private double mLastTime = 0d;

    public SimElevatorHardware(Clock pClock) {
        this.mClock = pClock;
    }

    public void init() {
        zero();
    }

    public void zero() {
        mEncoderEstimator.zero();

        mMasterSim.zero();

    }
    
    public void set(EElevatorPosition pDesiredPosition) {
        
    }

    public boolean checkHardware() {
        return true;
    }

}