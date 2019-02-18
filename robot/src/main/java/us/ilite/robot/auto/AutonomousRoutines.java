package us.ilite.robot.auto;

import com.team254.lib.geometry.Pose2dWithCurvature;
import com.team254.lib.trajectory.timing.CentripetalAccelerationConstraint;
import com.team254.lib.trajectory.timing.TimingConstraint;
import org.apache.commons.lang3.EnumUtils;
import us.ilite.common.Data;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.lib.trajectory.TrajectoryGenerator;
import us.ilite.common.types.auton.ECargoAction;
import us.ilite.common.types.auton.EHatchAction;
import us.ilite.common.types.auton.EStartingPosition;
import us.ilite.robot.auto.paths.middle.MiddleToMiddleCargoToMiddleCargo;
import us.ilite.robot.auto.paths.middle.MiddleToMiddleCargoToSideCargo;
import us.ilite.robot.auto.paths.middle.MiddleToMiddleCargoToSideRocket;
import us.ilite.robot.commands.*;
import us.ilite.robot.modules.*;

import java.util.Arrays;
import java.util.List;

public class AutonomousRoutines {

    public static final List<TimingConstraint<Pose2dWithCurvature>> kTrajectoryConstraints = Arrays.asList(new CentripetalAccelerationConstraint(40.0));
    public static final double kMaxVel = 100.0;
    public static final double kMaxAccel = 40.0;
    public static final double kMaxVoltage = 12.0;

    private TrajectoryGenerator mTrajectoryGenerator;

    private Drive mDrive;
    private Elevator mElevator;
    private Intake mIntake;
    private CargoSpit mCargoSpit;
    private HatchFlower mHatchFlower;
    private Limelight mLimelight;
    private Data mData;

    private MiddleToMiddleCargoToSideRocket mMiddleToMiddleCargoToSideRocket;
    private MiddleToMiddleCargoToMiddleCargo mMiddleToMiddleCargoToMiddleCargo;
    private MiddleToMiddleCargoToSideCargo mMiddleToMiddleCargoToSideCargo;

    private ICommand[] mMiddleToMiddleCargoToSideRocketSequence;
    private ICommand[] mMiddleToMiddleHatchToSideRocketSequence;
    private ICommand[] mMiddleToMiddleCargoToSideCargoSequence;
    private ICommand[] mMiddleToMiddleCargoToSideHatchSequence;
    private ICommand[] mMiddleToMiddleCargoMiddleCargoSequence;
    private ICommand[] mMiddleToMiddleCargoMiddleHatchSequence;

    public AutonomousRoutines(TrajectoryGenerator pTrajectoryGenerator, Drive pDrive, Elevator pElevator, Intake pIntake, CargoSpit pCargoSpit, HatchFlower pHatchFlower, Limelight pLimelight, Data pData) {
        mTrajectoryGenerator = pTrajectoryGenerator;
        mDrive = pDrive;
        mElevator = pElevator;
        mIntake = pIntake;
        mCargoSpit = pCargoSpit;
        mHatchFlower = pHatchFlower;
        mLimelight = pLimelight;
        mData = pData;

        mMiddleToMiddleCargoToSideRocket = new MiddleToMiddleCargoToSideRocket(mTrajectoryGenerator, mDrive, mLimelight, mData);
        mMiddleToMiddleCargoToMiddleCargo = new MiddleToMiddleCargoToMiddleCargo(mTrajectoryGenerator, mDrive, mLimelight, mData);
        mMiddleToMiddleCargoToSideCargo = new MiddleToMiddleCargoToSideCargo(mTrajectoryGenerator, mDrive, mLimelight, mData);
    }

    public void generateTrajectories() {
        //Cargo Sequences
        mMiddleToMiddleCargoToSideRocketSequence = mMiddleToMiddleCargoToSideRocket.generateCargoSequence();
        mMiddleToMiddleCargoToSideCargoSequence = mMiddleToMiddleCargoToSideCargo.generateCargoSequence();
        mMiddleToMiddleCargoMiddleCargoSequence = mMiddleToMiddleCargoToMiddleCargo.generateCargoSequence();

        //Hatch Sequences
        mMiddleToMiddleHatchToSideRocketSequence = mMiddleToMiddleCargoToSideRocket.generateHatchSequence();
        mMiddleToMiddleCargoToSideCargoSequence = mMiddleToMiddleCargoToSideCargo.generateHatchSequence();
        mMiddleToMiddleCargoMiddleCargoSequence = mMiddleToMiddleCargoToMiddleCargo.generateCargoSequence();
    }

    public ICommand[] getDefault() {
        return mMiddleToMiddleCargoToSideRocket.generateCargoSequence();
    }

    public ICommand[] getSequence() {
        Integer cargoAction = (int)SystemSettings.AUTON_TABLE.getEntry( ECargoAction.class.getSimpleName() ).getDouble( 99 );
        Integer hatchAction = (int)SystemSettings.AUTON_TABLE.getEntry( EHatchAction.class.getSimpleName() ).getDouble( 99 );
        Integer startingPosition = (int)SystemSettings.AUTON_TABLE.getEntry( EStartingPosition.class.getSimpleName() ).getDouble( 99 );

        switch(EStartingPosition.intToEnum( startingPosition )) {
            case LEFT:
            case RIGHT:
            case MIDDLE:
                switch ( ECargoAction.intToEnum( cargoAction ) ) {
                    case A:
                        return mMiddleToMiddleCargoToSideRocket.generateCargoSequence();
                        break;
                    case B:
                        return mMiddleToMiddleCargoToSideCargo.generateCargoSequence();
                        break;
                    case C:
                        return mMiddleToMiddleCargoToMiddleCargo.generateCargoSequence();
                        break;

                        default:
                            break;
                }
                switch ( EHatchAction.intToEnum( hatchAction )) {
                    case A:
                        return mMiddleToMiddleCargoToSideRocket.generateHatchSequence();
                        break;
                    case B:
                        return mMiddleToMiddleCargoToMiddleCargo.generateHatchSequence();
                        break;
                    case C:
                        return mMiddleToMiddleCargoToSideCargo.generateHatchSequence();
                        break;

                        default:
                            break;
                }
            case UNKNOWN:
        }

    }

}
