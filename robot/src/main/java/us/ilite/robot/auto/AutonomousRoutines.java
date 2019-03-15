package us.ilite.robot.auto;

import com.google.gson.Gson;
import com.team254.lib.trajectory.timing.CentripetalAccelerationConstraint;
import us.ilite.common.AutonSelectionData;
import us.ilite.common.Data;
import us.ilite.common.lib.trajectory.TrajectoryConstraints;
import us.ilite.common.lib.trajectory.TrajectoryGenerator;
import us.ilite.common.types.auton.*;
import us.ilite.robot.auto.paths.AutoSequence;
import us.ilite.lib.drivers.VisionGyro;
import us.ilite.robot.auto.paths.DefaultAuto;
import us.ilite.robot.auto.paths.left.LeftToRocketToRocket;
import us.ilite.robot.auto.paths.midLeft.MidLeftToFrontLeftToRocket;
import us.ilite.robot.auto.paths.midRight.MidRightToFrontRightToRocket;
import us.ilite.robot.auto.paths.right.RightToRocketToRocket;
import us.ilite.robot.commands.*;
import us.ilite.robot.modules.*;

public class AutonomousRoutines {

    public static final TrajectoryConstraints kDefaultTrajectoryConstraints = new TrajectoryConstraints(
            100.0,
            40.0,
            12.0,
            new CentripetalAccelerationConstraint(20.0)
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

    private AutoSequence mLeft_FrontLeft_Rocket;//TODO these
    private AutoSequence mMidLeft_FrontLeft_Rocket;//TODO these
    private AutoSequence mRight_FrontRight_Rocket;//TODO these
    private AutoSequence mMidRight_FrontRight_Rocket;//TODO these

    private ICommand[] mLeft_FrontLeft_Rocket_CargoSequence; //TODO these
    private ICommand[] mMidLeft_FrontLeft_Rocket_CargoSequence;//TODO these
    private ICommand[] mRight_FrontRight_Rocket_CargoSequence;//TODO these
    private ICommand[] mMidRight_FrontRight_Rocket_CargoSequence;//TODO these

    private ICommand[] mLeft_FrontLeft_Rocket_HatchSequence; //TODO these
    private ICommand[] mMidLeft_FrontLeft_Rocket_HatchSequence;//TODO these
    private ICommand[] mRight_FrontRight_Rocket_HatchSequence;//TODO these
    private ICommand[] mMidRight_FrontRight_Rocket_HatchSequence;//TODO these

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

        mLeft_FrontLeft_Rocket = new LeftToRocketToRocket(mTrajectoryGenerator, mData, mDrive, mHatchFlower, mPneumaticIntake, mCargoSpit, mElevator, mLimelight, mVisionGyro);
        mMidLeft_FrontLeft_Rocket = new MidLeftToFrontLeftToRocket(mTrajectoryGenerator, mData, mDrive, mHatchFlower, mPneumaticIntake, mCargoSpit, mElevator, mLimelight, mVisionGyro);
        mRight_FrontRight_Rocket = new RightToRocketToRocket(mTrajectoryGenerator, mData, mDrive, mHatchFlower, mPneumaticIntake, mCargoSpit, mElevator, mLimelight, mVisionGyro);
        mMidRight_FrontRight_Rocket = new MidRightToFrontRightToRocket(mTrajectoryGenerator, mData, mDrive, mHatchFlower, mPneumaticIntake, mCargoSpit, mElevator, mLimelight, mVisionGyro);

    }

    public void generateTrajectories() {
        //Cargo Sequences
        mLeft_FrontLeft_Rocket_CargoSequence = mLeft_FrontLeft_Rocket.generateCargoSequence();
        mMidLeft_FrontLeft_Rocket_CargoSequence = mMidLeft_FrontLeft_Rocket.generateCargoSequence();
        mRight_FrontRight_Rocket_CargoSequence = mRight_FrontRight_Rocket.generateCargoSequence();
        mMidRight_FrontRight_Rocket_CargoSequence = mMidRight_FrontRight_Rocket.generateCargoSequence();

        //Hatch Sequences
        mLeft_FrontLeft_Rocket_HatchSequence = mLeft_FrontLeft_Rocket.generateHatchSequence();
        mMidLeft_FrontLeft_Rocket_HatchSequence = mMidLeft_FrontLeft_Rocket.generateHatchSequence();
        mRight_FrontRight_Rocket_CargoSequence = mRight_FrontRight_Rocket.generateHatchSequence();
        mMidRight_FrontRight_Rocket_CargoSequence = mMidRight_FrontRight_Rocket.generateHatchSequence();
    }

    public ICommand[] getDefault() {
        return mDefaultAuto.generateDefaultSequence();
    }

    public ICommand[] getSequence() {
        Integer cargoShipAction = (int)Data.kAutonTable.getEntry( ECargoShipAction.class.getSimpleName() ).getDouble(99);
        Integer hatchShipAction = (int)Data.kAutonTable.getEntry( EHatchShipAction.class.getSimpleName() ).getDouble(99);;
        Integer cargoRocketAction = (int)Data.kAutonTable.getEntry( ECargoRocketAction.class.getSimpleName() ).getDouble( 99 );
        Integer hatchRocketAction = (int)Data.kAutonTable.getEntry( EHatchRocketAction.class.getSimpleName() ).getDouble( 99 );
        Integer startingPosition = (int)Data.kAutonTable.getEntry( EStartingPosition.class.getSimpleName() ).getDouble( 99 );
        AutonSelectionData data = mGson.fromJson("", AutonSelectionData.class);
        
        switch(EStartingPosition.intToEnum( startingPosition )) {
            case LEFT:
                switch (EHatchShipAction.intToEnum( hatchShipAction )) {
                    case FRONT_LEFT:
                        switch (EHatchRocketAction.intToEnum( hatchRocketAction )) {
                            case LEFT:
                                return mLeft_FrontLeft_Rocket_HatchSequence;
                            case RIGHT:
                                return mLeft_FrontLeft_Rocket_HatchSequence; // To be changed
                            default:
                                break;
                        }
                    default:
                        break;
                }
                switch (ECargoShipAction.intToEnum( cargoShipAction )) {
                    case FRONT_LEFT:
                        switch (ECargoRocketAction.intToEnum( cargoRocketAction )) {
                            case MID:
                                return mLeft_FrontLeft_Rocket_CargoSequence;
                            default:
                                break;
                        }
                }
            case MID_LEFT:
                switch (EHatchShipAction.intToEnum( hatchShipAction )) {
                    case FRONT_LEFT:
                        switch (EHatchRocketAction.intToEnum( hatchRocketAction )) {
                            case LEFT:
                                return mMidLeft_FrontLeft_Rocket_HatchSequence;
                            case RIGHT:
                                return mMidLeft_FrontLeft_Rocket_HatchSequence; // To be changed
                            default:
                                break;
                        }
                    default:
                        break;
                }
                switch (ECargoShipAction.intToEnum( cargoShipAction )) {
                    case FRONT_LEFT:
                        switch (ECargoRocketAction.intToEnum( cargoRocketAction )) {
                            case MID:
                                return mMidLeft_FrontLeft_Rocket_CargoSequence;
                            default:
                                break;
                        }
                }
            case MID_RIGHT:
                switch (EHatchShipAction.intToEnum( hatchShipAction )) {
                    case FRONT_RIGHT:
                        switch (EHatchRocketAction.intToEnum( hatchRocketAction )) {
                            case LEFT:
                                return mMidLeft_FrontLeft_Rocket_HatchSequence;
                            case RIGHT:
                                return mMidLeft_FrontLeft_Rocket_HatchSequence; // To be changed
                            default:
                                break;
                        }
                    default:
                        break;
                }
                switch (ECargoShipAction.intToEnum( cargoShipAction )) {
                    case FRONT_RIGHT:
                        switch (ECargoRocketAction.intToEnum( cargoRocketAction )) {
                            case MID:
                                return mMidLeft_FrontLeft_Rocket_CargoSequence;
                            default:
                                break;
                        }
                }
            case RIGHT:
                switch (EHatchShipAction.intToEnum( hatchShipAction )) {
                    case FRONT_RIGHT:
                        switch (EHatchRocketAction.intToEnum( hatchRocketAction )) {
                            case LEFT:
                                return mRight_FrontRight_Rocket_HatchSequence;
                            case RIGHT:
                                return mRight_FrontRight_Rocket_HatchSequence; // To be changed
                            default:
                                break;
                        }
                    default:
                        break;
                }
                switch (ECargoShipAction.intToEnum( cargoShipAction )) {
                    case FRONT_LEFT:
                        switch (ECargoRocketAction.intToEnum( cargoRocketAction )) {
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

}
