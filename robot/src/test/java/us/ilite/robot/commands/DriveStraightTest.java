package us.ilite.robot.commands;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.team254.lib.util.Util;
import net.bytebuddy.asm.Advice;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import us.ilite.common.Data;
import us.ilite.common.types.drive.EDriveData;
import us.ilite.common.types.sensor.EGyro;
import us.ilite.robot.modules.Drive;
import us.ilite.robot.modules.DriveMessage;


import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DriveStraightTest {

    @Mock Drive mDrive;

    Data mData = new Data();
    DriveStraight mDriveStraightCommand;

    /**
     * Verify % output mode starts, outputs, and stops correctly
     */
    @Test
    public void testPercentOutputDrive() {

        mDriveStraightCommand = new DriveStraight(
                mDrive, mData,
                DriveStraight.EDriveControlMode.PERCENT_OUTPUT,
                120.0);

        setYaw(90.0);
        setDistanceDriven(30.0, 50.0);
        mDriveStraightCommand.init(0.0);

        assertEquals(false, mDriveStraightCommand.update(0.0));

        setDistanceDriven(30.0 + 50.0, 50.0 + 50.0);
        assertEquals(false, mDriveStraightCommand.update(0.0));

        // Within error deadband
        setDistanceDriven(30.0 + 117.0, 50.0 + 117.0);
        assertEquals(true, mDriveStraightCommand.update(0.0));

    }

    /**
     * Verify motion magic mode starts, outputs, and stops correctly
     */
    @Test
    public void testMotionMagicDrive() {

        mDriveStraightCommand = new DriveStraight(mDrive, mData,
                DriveStraight.EDriveControlMode.MOTION_MAGIC,
                120.0);

        setYaw(90.0);
        setDistanceDriven(30.0, 50.0);
        mDriveStraightCommand.init(0.0);

        assertEquals(false, mDriveStraightCommand.update(0.0));

        setDistanceDriven(30.0 + 50.0, 50.0 + 50.0);
        assertEquals(false, mDriveStraightCommand.update(0.0));

        // Within error deadband
        setDistanceDriven(30.0 + 117.0, 50.0 + 117.0);
        assertEquals(true, mDriveStraightCommand.update(0.0));

    }

    /**
     * Verify % output mode works with both gyro heading and rate correction.
     */
    @Test
    public void testPercentOutputCorrection() {
        mDriveStraightCommand = new DriveStraight(
                mDrive, mData,
                DriveStraight.EDriveControlMode.PERCENT_OUTPUT,
                120.0);
        setYaw(90.0);
        setDistanceDriven(30.0, 50.0);
        mDriveStraightCommand.init(0.0);

        setYaw(180.0);
        mDriveStraightCommand.update(0.0);
        ArgumentCaptor<DriveMessage> captor = ArgumentCaptor.forClass(DriveMessage.class);
        verify(mDrive).setDriveMessage(captor.capture());
        assertEquals(-1.0, captor.getValue().leftDemand, Util.kEpsilon);
        assertEquals(1.0, captor.getValue().rightDemand, Util.kEpsilon);

    }

    /**
     * Verify motion magic mode works with both gyro heading and rate correction
     */
    @Test
    public void testMotionMagicDriveCorrection() {

        mDriveStraightCommand = new DriveStraight(
                mDrive, mData,
                DriveStraight.EDriveControlMode.MOTION_MAGIC,
                120.0);
        setYaw(90.0);
        setDistanceDriven(30.0, 50.0);
        mDriveStraightCommand.init(0.0);

        setYaw(180.0);

        mDriveStraightCommand.update(0.0);
        ArgumentCaptor<DriveMessage> captor = ArgumentCaptor.forClass(DriveMessage.class);
        verify(mDrive).setDriveMessage(captor.capture());
        assertEquals(-1.0, captor.getValue().leftDemand, Util.kEpsilon);
        assertEquals(1.0, captor.getValue().rightDemand, Util.kEpsilon);

    }

    private void setDistanceDriven(double pLeftDistance, double pRightDistance) {
        mData.drive.set(EDriveData.LEFT_POS_INCHES, pLeftDistance);
        mData.drive.set(EDriveData.RIGHT_POS_INCHES, pRightDistance);
    }

    private void setYaw(double pYaw) {
        mData.imu.set(EGyro.YAW_DEGREES, pYaw);
    }

}