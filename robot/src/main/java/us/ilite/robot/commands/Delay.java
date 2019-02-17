package us.ilite.robot.commands;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

public class Delay implements ICommand {
	private ILog mLog = Logger.createLog(Delay.class);

	private double mDelayDuration;
	private double triggeredTime;

	/**
	 * @param pDelayDuration in seconds
	 */
	public Delay(double pDelayDuration) {
		mDelayDuration = pDelayDuration;
	}

	@Override
	public void init(double pNow) {
	  	triggeredTime = pNow;
		mLog.info("Initializing delay with a duration of: ", mDelayDuration, " at: ", triggeredTime);
	}

	@Override
	public boolean update(double pNow) {
		if(pNow - triggeredTime < mDelayDuration) {
			return false;
		}
		return true;
		
	}


	@Override
	public void shutdown(double pNow) {

	}

}

