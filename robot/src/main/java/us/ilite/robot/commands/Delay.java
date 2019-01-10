package us.ilite.robot.commands;

public class Delay implements ICommand {
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

