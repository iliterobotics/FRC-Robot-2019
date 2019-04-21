package us.ilite.robot.auto;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import com.google.gson.Gson;
import com.team254.lib.trajectory.timing.CentripetalAccelerationConstraint;
import us.ilite.common.AutonSelectionData;
import us.ilite.common.Data;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.lib.trajectory.TrajectoryConstraints;
import us.ilite.common.lib.trajectory.TrajectoryGenerator;
import us.ilite.robot.auto.paths.AutoSequence;
import us.ilite.lib.drivers.VisionGyro;
import us.ilite.robot.auto.paths.DefaultAuto;
import us.ilite.robot.auto.paths.left.LeftToCargoShipToRocket;
import us.ilite.robot.auto.paths.left.LeftToRocket;
import us.ilite.robot.auto.paths.midLeft.MidLeftToCargoShipToRocket;
import us.ilite.robot.auto.paths.midRight.MidRightToCargoShipToRocket;
import us.ilite.robot.auto.paths.right.RightToCargoShipToRocket;
import us.ilite.robot.auto.paths.right.RightToRocket;
import us.ilite.robot.commands.*;
import us.ilite.robot.modules.*;

public class AutonomousRoutines {

    private final ILog mLogger = Logger.createLog(AutonomousRoutines.class);

    public static final TrajectoryConstraints kDefaultTrajectoryConstraints = new TrajectoryConstraints(
            130.0,
            130.0,
            12.0,
            new CentripetalAccelerationConstraint(100.0)
    );

    private TrajectoryGenerator mTrajectoryGenerator;

    private Drive mDrive;
    private Elevator mElevator;
    private Intake mIntake;
    private CargoSpit mCargoSpit;
    private HatchFlower mHatchFlower;
    private Limelight mLimelight;
    private VisionGyro mVisionGyro;
    private Data mData;

    private DefaultAuto mDefaultAuto;

    // Start far left
    private AutoSequence mLeftToCargoShipToRocket, mLeftToRocket;
    // Start mid left
    private AutoSequence mMidLeftCargoShipToRocket;
    // Start mid right
    private AutoSequence mMidRightToCargoShipToRocket;
    // Start far right
    private AutoSequence mRightToCargoShipToRocket, mRightToRocket;

    private Gson mGson = new Gson();

    public AutonomousRoutines(TrajectoryGenerator mTrajectoryGenerator, Drive mDrive, Elevator mElevator, PneumaticIntake mPneumaticIntake, Intake mIntake, CargoSpit mCargoSpit, HatchFlower mHatchFlower, Limelight mLimelight, VisionGyro mVisionGyro, Data mData) {
        this.mTrajectoryGenerator = mTrajectoryGenerator;
        this.mDrive = mDrive;
        this.mElevator = mElevator;
        this.mIntake = mIntake;
        this.mCargoSpit = mCargoSpit;
        this.mHatchFlower = mHatchFlower;
        this.mLimelight = mLimelight;
        this.mVisionGyro = mVisionGyro;
        this.mData = mData;

        // Start far left
        mLeftToCargoShipToRocket = new LeftToCargoShipToRocket(mTrajectoryGenerator, mData, mDrive, mHatchFlower, mPneumaticIntake, mCargoSpit, mElevator, mLimelight, mVisionGyro);
        mLeftToRocket = new LeftToRocket(mTrajectoryGenerator, mData, mDrive, mHatchFlower, mPneumaticIntake, mCargoSpit, mElevator, mLimelight, mVisionGyro);
        // Start mid left
        mMidLeftCargoShipToRocket = new MidLeftToCargoShipToRocket(mTrajectoryGenerator, mData, mDrive, mHatchFlower, mPneumaticIntake, mCargoSpit, mElevator, mLimelight, mVisionGyro);
        // Start mid right
        mMidRightToCargoShipToRocket = new MidRightToCargoShipToRocket(mTrajectoryGenerator, mData, mDrive, mHatchFlower, mPneumaticIntake, mCargoSpit, mElevator, mLimelight, mVisionGyro);
        // Start far right
        mRightToCargoShipToRocket = new RightToCargoShipToRocket(mTrajectoryGenerator, mData, mDrive, mHatchFlower, mPneumaticIntake, mCargoSpit, mElevator, mLimelight, mVisionGyro);
        mRightToRocket = new RightToRocket(mTrajectoryGenerator, mData, mDrive, mHatchFlower, mPneumaticIntake, mCargoSpit, mElevator, mLimelight, mVisionGyro);

    }

    public void generateTrajectories() {

    }

    public ICommand[] getDefault() {
        return mDefaultAuto.generateDefaultSequence();
    }

    public ICommand[] getSequence() {
        String jsonData = Data.kAutonTable.getEntry(SystemSettings.kAutonSelectionDataKey).getString("");
        AutonSelectionData data = mGson.fromJson(jsonData, AutonSelectionData.class);

        mLogger.warn("\n", data);

        switch(data.mStartingPosition) {
            case LEFT:
                switch (data.mHatchShipAction) {
                    case FRONT_LEFT:
                        switch (data.mHatchRocketAction) {
                            case FRONT:
                                return mLeft_FrontLeft_Rocket_HatchSequence;
                            case BACK:
                                return mLeft_FrontLeft_Rocket_HatchSequence; // To be changed
                            default:
                                break;
                        }
                    default:
                        break;
                }
                switch (data.mCargoShipAction) {
                    case FRONT_LEFT:
                        switch (data.mCargoRocketAction) {
                            case MID:
                                return mLeft_FrontLeft_Rocket_CargoSequence;
                            default:
                                break;
                        }
                }
            case MID_LEFT:
                switch (data.mHatchShipAction) {
                    case FRONT_LEFT:
                        switch (data.mHatchRocketAction) {
                            case FRONT:
                                return mMidLeft_FrontLeft_Rocket_HatchSequence;
                            case BACK:
                                return mMidLeft_FrontLeft_Rocket_HatchSequence; // To be changed
                            default:
                                break;
                        }
                    default:
                        break;
                }
                switch (data.mCargoShipAction) {
                    case FRONT_LEFT:
                        switch (data.mCargoRocketAction) {
                            case MID:
                                return mMidLeft_FrontLeft_Rocket_CargoSequence;
                            default:
                                break;
                        }
                }
            case MID_RIGHT:
                switch (data.mHatchShipAction) {
                    case FRONT_LEFT:
                        switch (data.mHatchRocketAction) {
                            case FRONT:
                                return mMidLeft_FrontLeft_Rocket_HatchSequence;
                            case BACK:
                                return mMidLeft_FrontLeft_Rocket_HatchSequence; // To be changed
                            default:
                                break;
                        }
                    default:
                        break;
                }
                switch (data.mCargoShipAction) {
                    case FRONT_LEFT:
                        switch (data.mCargoRocketAction) {
                            case MID:
                                return mMidLeft_FrontLeft_Rocket_CargoSequence;
                            default:
                                break;
                        }
                }
            case RIGHT:
                switch (data.mHatchShipAction) {
                    case FRONT_LEFT:
                        switch (data.mHatchRocketAction) {
                            case FRONT:
                                return mRight_FrontRight_Rocket_HatchSequence;
                            case BACK:
                                return mRight_FrontRight_Rocket_HatchSequence; // To be changed
                            default:
                                break;
                        }
                    default:
                        break;
                }
                switch (data.mCargoShipAction) {
                    case FRONT_LEFT:
                        switch (data.mCargoRocketAction) {
                            case MID:
                                return mRight_FrontRight_Rocket_CargoSequence;
                            default:
                                break;
                        }
                }
            case UNKNOWN:
                break;
            default:
                return mDefaultAuto.generateDefaultSequence();
        }

        return null;

    }

    public AutoSequence getmLeft_FrontLeft_Rocket() {
        return mLeft_FrontLeft_Rocket;
    }

    public AutoSequence getmMidLeft_FrontLeft_Rocket() {
        return mMidLeft_FrontLeft_Rocket;
    }

    public AutoSequence getmRight_FrontRight_Rocket() {
        return mRight_FrontRight_Rocket;
    }

    public AutoSequence getmMidRight_FrontRight_Rocket() {
        return mMidRight_FrontRight_Rocket;
    }

}
