package us.ilite.robot.commands;

import com.team254.lib.util.Util;
import us.ilite.robot.modules.Drive;
import us.ilite.robot.modules.DriveMessage;

public class MoveToDistance implements ICommand {
    private double mEpsilonInches = 1.0;
    private double mPower = 0.5;
    private final Drive drive;
    private final IAbsoluteDistanceProvider distanceProvider;
    private final double goalDistanceInInches;

    
    public MoveToDistance(Drive drive, IAbsoluteDistanceProvider distanceProvider, double goalDistanceInInches) {
        this.distanceProvider = distanceProvider;
        this.drive = drive;
        this.goalDistanceInInches = goalDistanceInInches;
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
        double deltaDistance = distanceProvider.getAbsoluteDistanceInInches() - goalDistanceInInches;
        if(Util.epsilonEquals(deltaDistance, 0.0, mEpsilonInches)) {
            isFinished = true;
        } else if(deltaDistance > 0.0d) {
            //Move backwards
            drive.setDriveMessage(DriveMessage.fromThrottleAndTurn(mPower, 0));
            isFinished = false;
        } else if(deltaDistance < 0.0d) {
            drive.setDriveMessage(DriveMessage.fromThrottleAndTurn(-mPower, 0));
        } else {
            //Should be impossible, but we'll say we're done
            isFinished = true;
        }
        
        return isFinished;
    }

    public MoveToDistance setEpsilonInches(double pEpsilonInches) {
        mEpsilonInches = pEpsilonInches;
        return this;
    }

    public MoveToDistance setPower(double pPower) {
        mPower = pPower;
        return this;
    }

}