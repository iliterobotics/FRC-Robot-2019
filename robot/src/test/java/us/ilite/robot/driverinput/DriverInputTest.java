package us.ilite.robot.driverinput;

import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.flybotix.hfr.util.log.ELevel;
import com.flybotix.hfr.util.log.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import us.ilite.TestingUtils;
import us.ilite.common.Data;
import us.ilite.common.config.DriveTeamInputMap;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.types.input.ELogitech310;
import us.ilite.lib.drivers.Clock;
import us.ilite.robot.commands.Delay;
import us.ilite.robot.modules.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DriverInputTest {

    // We're only testing integration between CommandManager and DriverInput, so we can mock this
    @Mock private Drive mDrive;
    @Mock private HatchFlower mHatchFlower;
    private CommandManager mAutonomousCommandManager;
    // We want to see CommandManager's actual behavior, so we make it a spy
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
        MockitoAnnotations.initMocks(this);
        Logger.setLevel(ELevel.DEBUG);

        mData = new Data();
        mClock = new Clock().simulated();
        mModuleList = new ModuleList();
        mTeleopCommandManager = spy(new CommandManager());
        mAutonomousCommandManager = spy(new CommandManager());
        mLimelight = new Limelight(mData);
        mDriverInput = spy(new DriverInput( mDrive, mElevator, mHatchFlower, mIntake, mPneumaticIntake, mCargoSpit, mLimelight, mData, mTeleopCommandManager, mAutonomousCommandManager, mFourBar, true ) );
        
        mModuleList.setModules(mDriverInput, mTeleopCommandManager, mAutonomousCommandManager, mDrive);
        mModuleList.modeInit(mClock.getCurrentTime());

        TestingUtils.fillNonButtons(mData.driverinput, mClock.getCurrentTime());
        TestingUtils.fillNonButtons(mData.operatorinput, mClock.getCurrentTime());
    }

    /**
     * Verify that the driver can stop autonomous and take manual control of the robot.
     */
    @Test
    public void testLeft_FrontLeft_Rocket_HatchSequence() {
        for(ELogitech310 overrideButton : SystemSettings.kAutonOverrideTriggers) {
            mData.driverinput.reset();
            // Reset superstructure with new command
            mAutonomousCommandManager.stopRunningCommands(mClock.getCurrentTime());
            mAutonomousCommandManager.startCommands(new Delay(30000.0));

            assertTrue(mAutonomousCommandManager.isRunningCommands());

            updateRobot();

            assertTrue(mAutonomousCommandManager.isRunningCommands());

            // Verify that we asked the superstructure to stop running commands when override is triggered
            mData.driverinput.set(overrideButton, 1.0);
            // Update twice to verify that commands aren't reset twice
            updateRobot();
            verify(mAutonomousCommandManager, times(2)).stopRunningCommands(anyDouble());

            // Verify that superstructure is actually stopped
            assertFalse(mAutonomousCommandManager.isRunningCommands());

            resetSpies();
        }
    }

    /**
     * Verify that the superstructure starts executing a command when the driver holds down a button
     * and stops executing when the driver releases the button.
     */
    @Test
    public void testTeleopDriverCommandHandling() {
        for(ELogitech310 commandTrigger : SystemSettings.kTeleopCommandTriggers) {
            mTeleopCommandManager.stopRunningCommands(mClock.getCurrentTime());
            mData.driverinput.set(commandTrigger, 1.0);
            updateRobot();

            verify(mDriverInput).updateVisionCommands(anyDouble());

            resetSpies();
        }
    }

    /**
     * Verify that when the driver starts a command while the superstructure is running one, the
     * current command is overridden.
     */
    @Test
    public void testAutonDriverCommandHandling() {

        for(ELogitech310 commandTrigger : SystemSettings.kTeleopCommandTriggers) {
            mAutonomousCommandManager.stopRunningCommands(0.0);
            mTeleopCommandManager.stopRunningCommands(0.0);
            mAutonomousCommandManager.startCommands(new Delay(30));
            // If we press and release a button the command queue should get stopped
            mData.driverinput.set(commandTrigger, 1.0);

            updateRobot();
            assertFalse(mAutonomousCommandManager.isRunningCommands());

            resetSpies();
        }

    }

    private void updateRobot() {
        mModuleList.update(mClock.getCurrentTime());
        mClock.cycleEnded();
    }

    private void updateRobot(int times) {
        for(int i = 1; i <= times; i++) updateRobot();
    }

    private void resetSpies() {
        Mockito.reset(mTeleopCommandManager, mAutonomousCommandManager, mDriverInput);
    }

}
