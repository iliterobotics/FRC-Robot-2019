package us.ilite.robot.modules;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import control.DriveController;
import us.ilite.common.types.drive.EDriveData;
import us.ilite.lib.drivers.Clock;
import us.ilite.robot.Data;
import us.ilite.robot.hardware.DriveHardware;
import us.ilite.robot.hardware.IDriveHardware;
import us.ilite.robot.hardware.SimDriveHardware;
import us.ilite.robot.loops.Loop;

/**
 * Class for running all drive train control operations from both autonomous and
 * driver-control
 */
public class Drive extends Loop {
	private final ILog mLogger = Logger.createLog(Drive.class);

	private Data mData;

	private IDriveHardware mDriveHardware;

	private EDriveState mDriveState;
	private DriveMessage mDriveMessage;

	public Drive(Data data, DriveController pDriveController, Clock pClock)
	{
		this.mData = data;
		this.mDriveHardware = new SimDriveHardware(pDriveController, pClock);
	}

	@Override
	public void modeInit(double pNow) {
	  	setDriveMessage(DriveMessage.kNeutral);
	  	setDriveState(EDriveState.NORMAL);
	}

	@Override
	public void periodicInput(double pNow) {
		mData.drive.set(EDriveData.LEFT_OUTPUT, mDriveMessage.leftOutput);
		mData.drive.set(EDriveData.RIGHT_OUTPUT, mDriveMessage.rightOutput);
		mData.drive.set(EDriveData.LEFT_POS_INCHES, mDriveHardware.getLeftInches());
		mData.drive.set(EDriveData.RIGHT_POS_INCHES, mDriveHardware.getRightInches());
		mData.drive.set(EDriveData.LEFT_VEL_IPS, mDriveHardware.getLeftVelInches());
		mData.drive.set(EDriveData.RIGHT_VEL_IPS, mDriveHardware.getRightInches());
	}

	@Override
	public void update(double pNow) {

		if(mDriveState != EDriveState.NORMAL) {
			mLogger.error("Invalid drive state - maybe you meant to run this a high frequency?");
		} else {
			mDriveHardware.set(mDriveMessage);
		}
	}
	
	@Override
	public void shutdown(double pNow) {
		mDriveHardware.zero();
	}

	@Override
	public void loop(double pNow) {
		update(pNow);
	}

	@Override
	public void checkModule(double pNow) {

	}

	public synchronized void zero() {
		mDriveHardware.zero();
	}
	
	public synchronized void setDriveMessage(DriveMessage pDriveMessage) {
		this.mDriveMessage = pDriveMessage;
	}

	public synchronized void setDriveState(EDriveState pDriveState) {
		this.mDriveState = pDriveState;
	}

	public synchronized IDriveHardware getDriveHardware() {
	    return mDriveHardware;
    }

}
	
	



	



  

	