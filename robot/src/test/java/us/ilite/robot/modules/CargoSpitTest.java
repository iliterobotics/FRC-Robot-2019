package us.ilite.robot.modules;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import us.ilite.common.Data;
import us.ilite.common.config.DriveTeamInputMap;
import us.ilite.lib.drivers.Clock;
import us.ilite.robot.driverinput.DriverInput;
import static org.junit.Assert.*;

import static org.mockito.Mockito.spy;

public class CargoSpitTest {

    private Data mData;
    private Clock mClock;
    private ModuleList mModuleList;
    private DriverInput mDriverInput;
    private Superstructure mSuperStructure;
    private HatchFlower mHatchFlower;
    private Elevator mElevator;
    private Drive mDrive;
    private CargoSpit mCargoSpit;


    @Before
    public void setup() {

        mData = new Data();
        mClock = new Clock().simulated();
        mModuleList = new ModuleList();
        mSuperStructure = spy( new Superstructure() );
        mCargoSpit = new CargoSpit( mData );

        mDriverInput = spy(new DriverInput( mDrive, mElevator, mHatchFlower, mCargoSpit, mSuperStructure, mData ));
        mModuleList.setModules( mDriverInput, mSuperStructure, mDrive );
        mModuleList.modeInit( mClock.getCurrentTime() );
    }

    @After
    public void cleanup() {
        mData = new Data();
        mClock = new Clock().simulated();
        mModuleList = new ModuleList();
        mSuperStructure = spy( new Superstructure() );
        mCargoSpit = new CargoSpit( mData );

        mDriverInput = spy(new DriverInput( mDrive, mElevator, mHatchFlower, mCargoSpit, mSuperStructure, mData ));
        mModuleList.setModules( mDriverInput, mSuperStructure, mDrive );
        mModuleList.modeInit( mClock.getCurrentTime() );
    }

    @Test
    public void testIntakeOneCycle() {
        mData.operatorinput.set( DriveTeamInputMap.MANIPULATOR_INTAKE_CARGO, 1.0 );
        updateRobot();
        assertTrue( mCargoSpit.ismIntaking() );

        mData.operatorinput.set( DriveTeamInputMap.MANIPULATOR_INTAKE_CARGO, null );
        updateRobot();
        assertFalse( mCargoSpit.ismIntaking() );
    }

    private void updateRobot() {
        mModuleList.update( mClock.getCurrentTime() );
        mClock.cycleEnded();
    }
}
