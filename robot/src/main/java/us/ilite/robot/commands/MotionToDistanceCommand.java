package us.ilite.robot.commands;

import us.ilite.robot.modules.Drive;
import us.ilite.robot.modules.DriveMessage;

public class MotionToDistanceCommand implements ICommand {
    private static final double DEFAULT_DISTANCE_BUFFER_IN_INCHES =12.0d;
    private final Drive drive;
    private final IDistanceProvider distanceProvider;
    private final double distanceBufferInInches;
    public MotionToDistanceCommand(Drive drive, IDistanceProvider distanceProvider) {
        this(drive, distanceProvider, DEFAULT_DISTANCE_BUFFER_IN_INCHES);
    }
    public MotionToDistanceCommand(Drive drive, IDistanceProvider distanceProvider, double distanceBufferInInches) {
        this.distanceProvider = distanceProvider;
        this.drive = drive;
        this.distanceBufferInInches = distanceBufferInInches;
    }
    @Override
    public void init(double pNow) {
    }

    @Override
    public void shutdown(double pNow) {
        
    }

    @Override
    public boolean update(double pNow) {
        boolean isFinished = false;
        //Take a measurment of how far the distance provider
        double distanceInIches = distanceProvider.getDistanceInches();
        if(distanceInIches > distanceInIches) {
            drive.setDriveMessage(DriveMessage.fromThrottleAndTurn(1.0, 0));
        } else if (distanceInIches < distanceInIches) {
            drive.setDriveMessage(DriveMessage.fromThrottleAndTurn(1.0, 0));
        } else {
            isFinished = true;
        }
        
        return isFinished;
    }

}