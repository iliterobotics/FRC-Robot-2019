package us.ilite.lib.drivers;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import edu.wpi.first.wpilibj.Timer;

/**
 * @author Stephen Welch
 * Provides a consistent time between cycles.
 * The cycleEnded() method must be called at the end of each robot cycle so we know when to update to the next time.
 */
public class Clock {

    private ILog mLogger = Logger.createLog(Clock.class);

    private double mCurrentTime = 0.0;
    private boolean hasTimeUpdatedThisCycle = false;

    /**
     *
     * @return A cycle-consistent time, in seconds.
     */
    public double getCurrentTime() {
        if(hasTimeUpdatedThisCycle == false) {
            mCurrentTime = Timer.getFPGATimestamp();
            hasTimeUpdatedThisCycle = true;
            mLogger.debug("Updated time to: " + mCurrentTime);
        }

        return mCurrentTime;
    }

    /**
     * Call this to signify the end of a robot cycle and tell the time to update next time it's retrieved.
     */
    public void cycleEnded() {
        hasTimeUpdatedThisCycle = false;
    }

}
