package us.ilite.robot.commands;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import us.ilite.common.lib.physics.DriveCharacterization;
import us.ilite.robot.modules.Drive;

import java.util.ArrayList;
import java.util.List;

public class CharacterizeDrive implements ICommand {

    private ILog mLog = Logger.createLog(CharacterizeDrive.class);

    private List<DriveCharacterization.VelocityDataPoint> mLeftVelData = new ArrayList<>();
    private List<DriveCharacterization.VelocityDataPoint> mRightVelData = new ArrayList<>();

    private List<DriveCharacterization.AccelerationDataPoint> mLeftAccelData = new ArrayList<>();
    private List<DriveCharacterization.AccelerationDataPoint> mRightAccelData = new ArrayList<>();


    private Drive mDrive;
    private CollectVelocityData mCollectVelocityData;
    private CollectAccelerationData mCollectAccelerationData;
    
    private CommandQueue mCommandQueue = new CommandQueue();

    public CharacterizeDrive(Drive pDrive, boolean pTurn, boolean pReverse) {
        mDrive = pDrive;
        mCollectVelocityData = new CollectVelocityData(mDrive, mLeftVelData, mRightVelData, pReverse, pTurn);
        mCollectAccelerationData = new CollectAccelerationData(mDrive, mLeftAccelData, mRightAccelData, pReverse, pTurn);
    }
    
    @Override
    public void init(double pNow) {
        mCommandQueue.setCommands(mCollectVelocityData, new Delay(3.0), mCollectAccelerationData);
        mCommandQueue.init(pNow);
    }

    @Override
    public boolean update(double pNow) {
        return mCommandQueue.update(pNow);
    }

    @Override
    public void shutdown(double pNow) {
        System.out.println("\nLeft:\n" + DriveCharacterization.characterizeDrive(mLeftVelData, mLeftAccelData));
        System.out.println("\nRight:\n" + DriveCharacterization.characterizeDrive(mRightVelData, mRightAccelData));
        mCommandQueue.shutdown(pNow);
    }
}
