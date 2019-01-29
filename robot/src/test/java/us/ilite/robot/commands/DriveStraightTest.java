package us.ilite.robot.commands;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import us.ilite.robot.Data;
import us.ilite.robot.modules.Drive;

@RunWith(MockitoJUnitRunner.class)
public class DriveStraightTest {

    @Mock Data mData;
    @Mock Drive mDrive;

    DriveStraight mDriveStraightCommand;

    /**
     * Verify % output mode starts, outputs, and stops correctly
     */
    @Test
    public void testPercentOutputDrive() {

    }

    /**
     * Verify motion magic mode starts, outputs, and stops correctly
     */
    @Test
    public void testMotionMagicDrive() {

    }

    /**
     * Verify % output mode works with both gyro heading and rate correction.
     */
    @Test
    public void testPercentOutputCorrection() {

    }

    /**
     * Verify motion magic mode works with both gyro heading and rate correction
     */
    @Test
    public void testMotionMagicDriveCorrection() {

    }

}