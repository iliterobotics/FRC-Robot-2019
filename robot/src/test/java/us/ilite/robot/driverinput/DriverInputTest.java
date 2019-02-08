package us.ilite.robot.driverinput;

import com.flybotix.hfr.util.log.ELevel;
import com.flybotix.hfr.util.log.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import us.ilite.common.Data;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.types.input.ELogitech310;
import us.ilite.lib.drivers.Clock;
import us.ilite.robot.commands.Delay;
import us.ilite.robot.modules.Drive;
import us.ilite.robot.modules.HatchFlower;
import us.ilite.robot.modules.ModuleList;
import us.ilite.robot.modules.Superstructure;
import us.ilite.robot.modules.Elevator;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DriverInputTest {

    // We're only testing integration between Superstructure and DriverInput, so we can mock this
    @Mock private Drive mDrive;
    @Mock private HatchFlower mHatchFlower;
    // We want to see Superstructure's actual behavior, so we make it a spy
    private Superstructure mSuperstructure;
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
        mSuperstructure = spy(new Superstructure());
        mDriverInput = spy(new DriverInput(mDrive, mElevator, mHatchFlower, mSuperstructure, mData));

        mModuleList.setModules(mDriverInput, mSuperstructure, mDrive);
        mModuleList.modeInit(mClock.getCurrentTime());
    }

    /**
     * Verify that the driver can stop autonomous and take manual control of the robot.
     */
    @Test
    public void testAutonomousOverride() {
        for(ELogitech310 overrideButton : SystemSettings.kAutonOverrideTriggers) {
            // Reset superstructure with new command
            mSuperstructure.startCommands(new Delay(30.0));
            assertTrue(mSuperstructure.isRunningCommands());

            // Verify that we asked the superstructure to stop running commands when override is triggered
            mData.driverinput.set(overrideButton, 1.0);
            // Update twice to verify that commands aren't reset twice
            updateRobot(2);
            verify(mSuperstructure).stopRunningCommands();

            // Verify that superstructure is actually stopped
            assertFalse(mSuperstructure.isRunningCommands());

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

            verify(mDriverInput).updateVisionCommands();
            assertTrue(mSuperstructure.isRunningCommands());

            mData.driverinput.set(commandTrigger, null);
            updateRobot();
            assertFalse(mSuperstructure.isRunningCommands());

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

            // Start commands externally
            mSuperstructure.startCommands(new Delay(30.0));
            updateRobot();
            assertTrue(mSuperstructure.isRunningCommands());

            // If we press and release a button the command queue should get stopped
            mData.driverinput.set(commandTrigger, 1.0);
            // Update twice to verify that commands aren't reset twice
            updateRobot(2);
            verify(mSuperstructure).stopRunningCommands();
            assertTrue(mSuperstructure.isRunningCommands());

            mData.driverinput.set(commandTrigger, null);
            updateRobot();
            verify(mSuperstructure, times(2)).stopRunningCommands();
            assertFalse(mSuperstructure.isRunningCommands());

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
        Mockito.reset(mSuperstructure, mDriverInput);
    }

}
