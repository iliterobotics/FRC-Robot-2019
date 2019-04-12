package us.ilite.robot.auto;

import com.flybotix.hfr.util.log.ELevel;
import com.flybotix.hfr.util.log.Logger;
import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import us.ilite.TestingUtils;
import us.ilite.common.AutonSelectionData;
import us.ilite.common.Data;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.lib.trajectory.TrajectoryGenerator;
import us.ilite.common.types.auton.*;
import us.ilite.lib.drivers.Clock;
import us.ilite.lib.drivers.VisionGyro;
import us.ilite.robot.auto.paths.left.LeftToRocketToRocket;
import us.ilite.robot.commands.ICommand;
import us.ilite.robot.driverinput.DriverInput;
import us.ilite.robot.modules.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;

@RunWith(MockitoJUnitRunner.class)
public class AutonomousRoutinesTest {

    @Mock private TrajectoryGenerator mTrajectoryGenerator;
    @Mock private Drive mDrive;
    @Mock private Elevator mElevator;
    @Mock private Intake mIntake;
    @Mock private PneumaticIntake mPneumaticIntake;
    @Mock private CargoSpit mCargoSpit;
    @Mock private HatchFlower mHatchFlower;
    @Mock private Limelight mLimelight;
    @Mock private VisionGyro mVisionGyro;
    @Mock private Data mData;

    private Gson mGson = new Gson();
    private AutonomousRoutines mAutonomousRoutines;
    private AutonSelectionData mAutonSelectionData;


    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);
        mAutonomousRoutines = new AutonomousRoutines(mTrajectoryGenerator, mDrive, mElevator, mPneumaticIntake, mIntake, mCargoSpit, mHatchFlower, mLimelight, mVisionGyro, mData);

    }

    @Test
    public void testLeft_FrontLeft_Rocket_HatchSequence() {
        mAutonSelectionData = new AutonSelectionData(
                ECargoRocketAction.NONE,
                ECargoShipAction.NONE,
                EHatchRocketAction.LEFT,
                EHatchShipAction.FRONT_LEFT,
                EStartingPosition.LEFT
        );
        Data.kAutonTable.getEntry(SystemSettings.kAutonSelectionDataKey).setString(mGson.toJson(mAutonSelectionData));
        assertEquals(mAutonomousRoutines.getmLeft_FrontLeft_Rocket().generateHatchSequence(), mAutonomousRoutines.getSequence());
        System.out.println("*****" + mAutonomousRoutines.getmLeft_FrontLeft_Rocket().generateHatchSequence().toString());
    }

    @Test
    public void testLeft_FrontLeft_Rocket_CargoSequence() {
        mAutonSelectionData = new AutonSelectionData(
                ECargoRocketAction.MID,
                ECargoShipAction.FRONT_LEFT,
                EHatchRocketAction.NONE,
                EHatchShipAction.NONE,
                EStartingPosition.LEFT
        );
        Data.kAutonTable.getEntry(SystemSettings.kAutonSelectionDataKey).setString(mGson.toJson(mAutonSelectionData));
        assertEquals(mAutonomousRoutines.getmLeft_FrontLeft_Rocket().generateCargoSequence(), mAutonomousRoutines.getSequence());
    }


}