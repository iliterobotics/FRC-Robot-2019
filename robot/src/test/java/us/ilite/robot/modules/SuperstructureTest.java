package us.ilite.robot.modules;

import com.flybotix.hfr.util.log.ELevel;
import com.flybotix.hfr.util.log.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import us.ilite.TestingUtils;
import us.ilite.common.Data;
import us.ilite.common.config.DriveTeamInputMap;
import us.ilite.lib.drivers.Clock;
import us.ilite.robot.driverinput.DriverInput;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SuperstructureTest {

    @Mock private Drive mDrive;
    @Mock private Elevator mElevator;
    @Mock private CargoSpit mCargoSpit;
    @Mock private HatchFlower mHatchFlower;
    @Mock private Intake mIntake;

    private Superstructure mSuperstructure;

    private Data mData;
    private Clock mClock;
    private DriverInput mDriverInput;
    private ModuleList mModuleList;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Logger.setLevel(ELevel.DEBUG);

        mData = new Data();
        mClock = new Clock().simulated();
        mModuleList = new ModuleList();
        mSuperstructure = spy(new Superstructure(mElevator, mIntake, mHatchFlower, mCargoSpit));
        mDriverInput = spy(new DriverInput(mDrive, mSuperstructure, mData, true));

        mModuleList.setModules(mDriverInput, mSuperstructure);
        mModuleList.modeInit(mClock.getCurrentTime());

        TestingUtils.fillNonButtons(mData.driverinput, 0.0);
        TestingUtils.fillNonButtons(mData.operatorinput, 0.0);
    }

    /**
     * Test that the superstructure transitions from ground hatch intake to hatch handoff correctly.
     */
    @Test
    public void testHatchHandoff() {

        when(mIntake.hasHatch()).thenReturn(false);
        when(mElevator.isAtPosition(EElevatorPosition.BOTTOM)).thenReturn(false);
        updateRobot(3);

        mData.operatorinput.set(DriveTeamInputMap.OPERATOR_INTAKE_GROUND_HATCH_AXIS, -1.0);
        updateRobot(3);

        assertEquals(Superstructure.EAcquisitionState.GROUND_HATCH, mSuperstructure.getAcquisitionState());

        when(mElevator.isAtPosition(EElevatorPosition.BOTTOM)).thenReturn(false);
        when(mHatchFlower.isExtended()).thenReturn(true);
        updateRobot(3);

        assertEquals(Superstructure.EAcquisitionState.GROUND_HATCH, mSuperstructure.getAcquisitionState());

        when(mIntake.hasHatch()).thenReturn(true);
        when(mElevator.isAtPosition(EElevatorPosition.BOTTOM)).thenReturn(true);
        updateRobot(3);

        assertEquals(Superstructure.EAcquisitionState.HANDOFF, mSuperstructure.getAcquisitionState());

        when(mHatchFlower.hasHatch()).thenReturn(true);
        updateRobot(3);

        assertEquals(Superstructure.EAcquisitionState.STOWED, mSuperstructure.getAcquisitionState());

    }

    /**
     * Test that the superstructure transitions from ground cargo intake to hatch handoff correctly.
     */
    @Test
    public void testCargoHandoff() {

    }

    /**
     * Test that manual extension of the hatch grabber triggers correctly.
     */
    @Test
    public void testManualHatchGrabberExtend() {

    }

    /**
     * Test that the manual hatch grab only triggers when we aren't performing the handoff sequence.
     */
    @Test
    public void testManualHatchGrabberGrab() {

    }

    private void updateRobot() {
        mModuleList.update(mClock.getCurrentTime());
        mClock.cycleEnded();
    }

    private void updateRobot(int times) {
        for(int i = 1; i <= times; i++) updateRobot();
    }

}
