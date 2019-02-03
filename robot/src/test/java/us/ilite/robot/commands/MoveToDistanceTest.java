package us.ilite.robot.commands;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import us.ilite.robot.modules.Drive;
import us.ilite.robot.modules.DriveMessage;

public class MoveToDistanceTest {

    private MoveToDistance distanceToCommand;
    private Drive drive;
    private IAbsoluteDistanceProvider absoluteDistanceProvider;
    private final double DESIRED_DISTANCE_IN_INCHES = 25;

    @Before
    public void init() {
        drive = mock(Drive.class);
        absoluteDistanceProvider = mock(IAbsoluteDistanceProvider.class);
        distanceToCommand = new MoveToDistance(drive, absoluteDistanceProvider,DESIRED_DISTANCE_IN_INCHES);
    }
    @Test
    public void testSensorReadingGreater() {
        when(absoluteDistanceProvider.getAbsoluteDistanceInInches()).thenReturn(DESIRED_DISTANCE_IN_INCHES * 2);

        boolean isDone = distanceToCommand.update(System.currentTimeMillis());

        assertFalse(isDone);
        verify(drive, times(1)).setDriveMessage(argThat(getArgMatcher(0.5, 0.5)));

    }

    @Test
    public void testSensorReadingLess() {
        when(absoluteDistanceProvider.getAbsoluteDistanceInInches()).thenReturn(DESIRED_DISTANCE_IN_INCHES * -2);

        boolean isDone = distanceToCommand.update(System.currentTimeMillis());

        assertFalse(isDone);
        verify(drive, times(1)).setDriveMessage(argThat(getArgMatcher(-0.5, -0.5)));

    }

    @Test
    public void testSensorReading() {
        when(absoluteDistanceProvider.getAbsoluteDistanceInInches()).thenReturn(DESIRED_DISTANCE_IN_INCHES);

        boolean isDone = distanceToCommand.update(System.currentTimeMillis());

        assertTrue(isDone);
        verify(drive, never()).setDriveMessage(any(DriveMessage.class));

    }

    private static ArgumentMatcher<DriveMessage> getArgMatcher(double leftOutput, double rightOutput) {
        return (driverMessage)->{
            return driverMessage.leftOutput == leftOutput && driverMessage.rightOutput == rightOutput;
        };
    }
}