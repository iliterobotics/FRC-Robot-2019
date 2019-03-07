package us.ilite.robot.auto;

import com.google.gson.Gson;
import com.team254.lib.trajectory.timing.CentripetalAccelerationConstraint;
import us.ilite.common.AutonSelectionData;
import us.ilite.common.Data;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.lib.trajectory.TrajectoryConstraints;
import us.ilite.common.lib.trajectory.TrajectoryGenerator;
import us.ilite.common.types.auton.*;
import us.ilite.robot.auto.paths.AutoSequence;
import us.ilite.robot.auto.paths.DefaultAuto;
import us.ilite.robot.auto.paths.middle.MiddleToMiddleCargoToMiddleCargo;
import us.ilite.robot.auto.paths.middle.MiddleToMiddleCargoToSideCargo;
import us.ilite.lib.drivers.VisionGyro;
import us.ilite.robot.auto.paths.middle.MiddleToMiddleCargoToSideRocket;
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

    private AutoSequence mMiddleToMiddleCargoToSideRocket;

    private AutoSequence mMid_MidLeftCargo_LeftRocket;//TODO these
    private AutoSequence mMid_MidRightCargo_LeftRocket;//TODO these
    private AutoSequence mMid_MidRightCargo_LeftCargoShip;//TODO these
    private AutoSequence mMid_MidLeftCargo_LeftCargoShip;//TODO these

    private AutoSequence mMiddleToMiddleCargoToMiddleCargo;
    private AutoSequence mMiddleToMiddleCargoToSideCargo;
    private DefaultAuto mDefaultAuto;

    private ICommand[] mMiddleToMiddleCargoToSideRocketSequence;
    private ICommand[] mMiddleToMiddleHatchToSideRocketSequence;
    private ICommand[] mMiddleToMiddleCargoToSideCargoSequence;
    private ICommand[] mMiddleToMiddleCargoToSideHatchSequence;
    private ICommand[] mMiddleToMiddleCargoMiddleCargoSequence;
    private ICommand[] mMiddleToMiddleCargoMiddleHatchSequence;

    private ICommand[] mMid_MidLeftCargo_LeftRocket_Sequence; //TODO these
    private ICommand[] mMid_MidLeftCargo_LeftCargoShip_Sequence;//TODO these
    private ICommand[] mMid_MidRightCargo_LeftRocket_Sequence;//TODO these
    private ICommand[] mMid_MidRightCargo_LeftCargoShip_Sequence;//TODO these

    private Gson mGson = new Gson();

    public AutonomousRoutines(TrajectoryGenerator mTrajectoryGenerator, Drive mDrive, Elevator mElevator, Intake mIntake, CargoSpit mCargoSpit, HatchFlower mHatchFlower, Limelight mLimelight, VisionGyro mVisionGyro, Data mData) {
        this.mTrajectoryGenerator = mTrajectoryGenerator;
        this.mDrive = mDrive;
        this.mElevator = mElevator;
        this.mIntake = mIntake;
        this.mCargoSpit = mCargoSpit;
        this.mHatchFlower = mHatchFlower;
        this.mLimelight = mLimelight;
        this.mVisionGyro = mVisionGyro;
        this.mData = mData;

//        mMiddleToMiddleCargoToMiddleCargo = new MiddleToMiddleCargoToMiddleCargo(mTrajectoryGenerator, mDrive, mLimelight, mData);
//        mMiddleToMiddleCargoToSideCargo = new MiddleToMiddleCargoToSideCargo(mTrajectoryGenerator, mDrive, mLimelight, mData);
        this.mMiddleToMiddleCargoToSideRocket = new MiddleToMiddleCargoToSideRocket(mTrajectoryGenerator, mData, mDrive, mLimelight, mVisionGyro);
    }

    public void generateTrajectories() {
        //Cargo Sequences
        mMiddleToMiddleCargoToSideRocketSequence = mMiddleToMiddleCargoToSideRocket.generateCargoSequence();
        mMiddleToMiddleCargoToSideCargoSequence = mMiddleToMiddleCargoToSideCargo.generateCargoSequence();
        mMiddleToMiddleCargoMiddleCargoSequence = mMiddleToMiddleCargoToMiddleCargo.generateCargoSequence();

        //---------------------------------------------------------------------------------------------
        mMid_MidLeftCargo_LeftCargoShip_Sequence = mMid_MidLeftCargo_LeftRocket.generateCargoSequence();
        mMid_MidLeftCargo_LeftRocket_Sequence = mMid_MidLeftCargo_LeftCargoShip.generateCargoSequence();
        mMid_MidRightCargo_LeftCargoShip_Sequence = mMid_MidRightCargo_LeftCargoShip.generateCargoSequence();
        mMid_MidRightCargo_LeftRocket_Sequence = mMid_MidRightCargo_LeftRocket.generateCargoSequence();
        //---------------------------------------------------------------------------------------------


        //Hatch Sequences
        mMiddleToMiddleHatchToSideRocketSequence = mMiddleToMiddleCargoToSideRocket.generateHatchSequence();
        mMiddleToMiddleCargoToSideCargoSequence = mMiddleToMiddleCargoToSideCargo.generateHatchSequence();
        mMiddleToMiddleCargoMiddleCargoSequence = mMiddleToMiddleCargoToMiddleCargo.generateCargoSequence();
    }

    public ICommand[] getDefault() {
        return mMiddleToMiddleCargoToSideRocket.generateCargoSequence();
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
                break;
            case RIGHT:
                break;
            case MIDDLE:
                switch (ECargoShipAction.intToEnum( cargoShipAction )) {
                    case FRONT_LEFT:
                        switch (ECargoRocketAction.intToEnum( cargoRocketAction )) {
                            case FRONT:
                            case LEFT:
                                return mMid_MidLeftCargo_LeftRocket_Sequence;
                            case RIGHT:
                            default:
                                break;
                        }
                    case FRONT_RIGHT:
                        switch (ECargoRocketAction.intToEnum( cargoRocketAction )) {
                            case FRONT:
                            case LEFT:
                            case RIGHT:
                            default:
                                break;
                        }
                        default:
                            break;
                }

                switch (EHatchShipAction.intToEnum( hatchShipAction )) {
                    case FRONT:
                    case LEFT:
                    case RIGHT:
                }
                switch (EHatchRocketAction.intToEnum( hatchRocketAction )) {
                    case FRONT:
                    case LEFT:
                    case RIGHT:
                }
            case UNKNOWN:
            default:
                return mDefaultAuto.generateDefaultSequence();
        }

        return null;

    }

}
