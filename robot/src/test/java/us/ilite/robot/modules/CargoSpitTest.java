package us.ilite.robot.modules;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import us.ilite.common.Data;
import us.ilite.common.config.DriveTeamInputMap;
import us.ilite.lib.drivers.Clock;
import us.ilite.robot.driverinput.DriverInput;
import static org.junit.Assert.*;

import static org.mockito.Mockito.spy;

public class CargoSpitTest {

    // We're only testing integration between CommandManager and DriverInput, so we can mock this
    @Mock private Drive mDrive;
    @Mock private HatchFlower mHatchFlower;
    private CommandManager mAutonomousCommandManager;
    private CommandManager mTeleopCommandManager;
    @Mock private FourBar mFourBar;
    @Mock private Elevator mElevator;
    @Mock private Intake mIntake;
    @Mock private CargoSpit mCargospit;
    @Mock private Arm mArm;
    @Mock private TalonSRX mTalon;
    @Mock private PneumaticIntake mPneumaticIntake;
    @Mock private CargoSpit mCargoSpit;

    private DriverInput mDriverInput;
    private Limelight mLimelight;

    private Data mData;
    private Clock mClock;
    private ModuleList mModuleList;

    @Before
    public void setup() {

        mData = new Data();
        mClock = new Clock().simulated();
        mModuleList = new ModuleList();
        mCargoSpit = new CargoSpit( mData );
        mIntake = new Intake( mData );
        mArm = new MotionMagicArm();
        mLimelight = new Limelight( mData );
        mAutonomousCommandManager = new CommandManager();
        mPneumaticIntake = new PneumaticIntake( mData );

        mDriverInput = spy(new DriverInput( mDrive, mElevator, mHatchFlower, mIntake, mPneumaticIntake, mCargoSpit, mLimelight, mData, mTeleopCommandManager, mAutonomousCommandManager, mFourBar) );
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

        mDriverInput = spy(new DriverInput( mDrive, mElevator, mHatchFlower, mIntake, mPneumaticIntake, mCargoSpit, mLimelight, mData, mTeleopCommandManager, mAutonomousCommandManager, mFourBar, true));
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
