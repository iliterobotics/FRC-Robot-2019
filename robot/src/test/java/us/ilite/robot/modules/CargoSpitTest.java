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
    private HatchFlower mHatchFlower;
    private Elevator mElevator;
    private Drive mDrive;
    private CargoSpit mCargoSpit;
    private Intake mIntake;
    private PneumaticIntake mPneumaticIntake;
    private Limelight mLimelight;
    private Arm mArm;
    private CommandManager mCommandManager;
    private CommandManager mAutoCommandManager;


    @Before
    public void setup() {

        mData = new Data();
        mClock = new Clock().simulated();
        mModuleList = new ModuleList();
        mCargoSpit = new CargoSpit( mData );
        mIntake = new Intake( mData );
        mArm = new MotionMagicArm();
        mLimelight = new Limelight( mData );
        mAutoCommandManager = new CommandManager();
        mPneumaticIntake = new PneumaticIntake( mData );

        mDriverInput = spy(new DriverInput( mDrive, mElevator, mHatchFlower, mIntake, mPneumaticIntake, mCargoSpit, mLimelight, mData, mCommandManager, mAutoCommandManager) );
        mModuleList.setModules( mDriverInput, mDrive );
        mModuleList.modeInit( mClock.getCurrentTime() );
    }

    @After
    public void cleanup() {
        mData = new Data();
        mClock = new Clock().simulated();
        mModuleList = new ModuleList();
        mCargoSpit = new CargoSpit( mData );
        mIntake = new Intake( mData );
        mArm = new MotionMagicArm();

        mDriverInput = spy(new DriverInput( mDrive, mElevator, mHatchFlower, mIntake, mPneumaticIntake, mCargoSpit, mLimelight, mData, mCommandManager, mAutoCommandManager, true));
        mModuleList.setModules( mDriverInput, mDrive );
        mModuleList.modeInit( mClock.getCurrentTime() );
    }

//    @Test
//    public void testIntakeOneCycle() {
//        mData.operatorinput.set( DriveTeamInputMap.OPERATOR_CARGO_SELECT, 1.0 );
//        updateRobot();
//        assertTrue( mCargoSpit.isIntaking() );
//
//        mData.operatorinput.set( DriveTeamInputMap.OPERATOR_CARGO_SELECT, null );
//        updateRobot();
//        assertFalse( mCargoSpit.isIntaking() );
//    }

    private void updateRobot() {
        mModuleList.update( mClock.getCurrentTime() );
        mClock.cycleEnded();
    }
}
