package us.ilite.lib.drivers;

import edu.wpi.first.wpilibj.DigitalInput;

/**
 * Wrapper class for a beam-break sensor.
 */
public class DigitalBeamSensor  {

	private final DigitalInput mBeamInput;
	private final boolean mBeamBrokenState; //  Whether the sensor normally returns "true" or "false" when the beam is broken.
	
	public DigitalBeamSensor(int pInputChannel, boolean pBeamBrokenState) {
		mBeamInput = new DigitalInput(pInputChannel);
		mBeamBrokenState = pBeamBrokenState;
	}

	public DigitalBeamSensor(int pInputChannel) {
		this(pInputChannel, true);
	}

	/**
	 *
	 * @return Returns true if the beam is broken.
	 */
	public boolean isBroken() {
	  // NOTE - if the beam is noisy, we can do some filtered average based upon the leading
	  // edge of the detection rather than just a 'get'.  This effectively debounces the signal.
	  //	  mBeamInput.readRisingTimestamp()
		boolean reading =  mBeamInput.get();

		if(reading == true && mBeamBrokenState == true) {
			return true;
		} else if(reading == false && mBeamBrokenState == false) {
			return false;
		} else {
			return true;
		}

	}
	
}
