package us.ilite.robot.driverinput;

import com.flybotix.hfr.util.log.ELevel;
import com.flybotix.hfr.util.log.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import us.ilite.TestingUtils;
import us.ilite.common.Data;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.types.input.ELogitech310;
import us.ilite.lib.drivers.Clock;
import us.ilite.robot.commands.Delay;
import us.ilite.robot.modules.*;
import us.ilite.robot.modules.CommandManager;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DriverInputTest {

    // We're only testing integration between CommandManager and DriverInput, so we can mock this
    @Mock private Drive mDrive;
    @Mock private HatchFlower mHatchFlower;
    @Mock private CommandManager mAutonomousCommandManager;
    // We want to see CommandManager's actual behavior, so we make it a spy
    private CommandManager mTeleopCommandManager;
    @Mock private Elevator mElevator;


    private DriverInput mDriverInput;

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
        mDriverInput = spy(new DriverInput(mDrive, mElevator, mHatchFlower, mTeleopCommandManager, mAutonomousCommandManager, mData));

        mModuleList.setModules(mDriverInput, mTeleopCommandManager, mDrive);
        mModuleList.modeInit(mClock.getCurrentTime());

        TestingUtils.fillNonButtons(mData.driverinput, 0.0);
        TestingUtils.fillNonButtons(mData.operatorinput, 0.0);
    }

    /**
     * Verify that the driver can stop autonomous and take manual control of the robot.
     */
    @Test
    public void testAutonomousOverride() {
        for(ELogitech310 overrideButton : SystemSettings.kAutonOverrideTriggers) {
            // Reset superstructure with new command
            mTeleopCommandManager.startCommands(new Delay(30.0));
            assertTrue(mTeleopCommandManager.isRunningCommands());

            // Verify that we asked the superstructure to stop running commands when override is triggered
            mData.driverinput.set(overrideButton, 1.0);
            // Update twice to verify that commands aren't reset twice
            updateRobot(2);
            verify(mTeleopCommandManager).stopRunningCommands();

            // Verify that superstructure is actually stopped
            assertFalse(mTeleopCommandManager.isRunningCommands());

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
            mData.driverinput.set(commandTrigger, 1.0);
            // Update twice to verify that commands aren't reset twice
            updateRobot(2);

            verify(mDriverInput, times(2)).updateVisionCommands();
            assertTrue(mTeleopCommandManager.isRunningCommands());

            mData.driverinput.set(commandTrigger, null);
            updateRobot();
            assertFalse(mTeleopCommandManager.isRunningCommands());

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

            // If we press and release a button the command queue should get stopped
            mData.driverinput.set(commandTrigger, 1.0);
            // Update twice to verify that commands aren't reset twice
            updateRobot();
            verify(mTeleopCommandManager).stopRunningCommands();
            assertTrue(mTeleopCommandManager.isRunningCommands());

            mData.driverinput.set(commandTrigger, null);
            updateRobot();
            verify(mTeleopCommandManager, times(2)).stopRunningCommands();
            assertFalse(mTeleopCommandManager.isRunningCommands());

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
        Mockito.reset(mTeleopCommandManager, mDriverInput);
    }

}
