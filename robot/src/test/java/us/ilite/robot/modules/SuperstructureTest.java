package us.ilite.robot.modules;

import com.flybotix.hfr.util.log.ELevel;
import com.flybotix.hfr.util.log.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import us.ilite.common.Data;
import us.ilite.lib.drivers.Clock;
import us.ilite.robot.driverinput.DriverInput;

import static org.mockito.Mockito.spy;

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
        mDriverInput = spy(new DriverInput(mDrive, mSuperstructure, mData));

        mModuleList.setModules(mDriverInput, mSuperstructure);
        mModuleList.modeInit(mClock.getCurrentTime());
    }

    /**
     * Test that the superstructure transitions from ground hatch intake to hatch handoff correctly.
     */
    @Test
    public void testHatchHandoff() {

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

}
