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

    private double mStartTime = 0.0;
    private double mCurrentTime = 0.0;
    private boolean hasTimeUpdatedThisCycle = false;
    private boolean mIsSimulated = false;

    private boolean mIsPaused = false;
    private double mPauseStartTime = 0.0;

    public Clock() {
    }

    /**
     *
     * @return A cycle-consistent time, in seconds.
     */
    public double getCurrentTime() {
        if(hasTimeUpdatedThisCycle == false) {
            mCurrentTime = getTime();
            mCurrentTime -= mStartTime;
            hasTimeUpdatedThisCycle = true;
        }

        return mCurrentTime;
    }

    /**
     *
     * @return A cycle-consistent time, in milliseconds.
     */
    public double getCurrentTimeInMillis() {
        return getCurrentTime() * 1000;
    }

    /**
     *
     * @return A cycle-consistent time, in microseconds.
     */
    public double getCurrentTimeInMicros() {
        return getCurrentTime() * 1000000;
    }

    /**
     *
     * @return A cycle-consistent time, in nanoseconds.
     */
    public double getCurrentTimeInNanos() {
        return getCurrentTime() * 1000000000;
    }

    /**
     * Call this to signify the end of a robot cycle and tell the time to update next time it's retrieved.
     */
    public void cycleEnded() {
        hasTimeUpdatedThisCycle = false;
    }

    public void setTime(double time) {
        if(mIsSimulated) {
            mStartTime = 0.0;
            mCurrentTime = time;
        } else {
            mLogger.error("Setting the current time is not allowed outside of simulation.");
        }
    }

    private double getTime() {
        return (mIsSimulated) ? getJavaTime() : getRobotTime();
    }

    public Clock simulated() {
        mStartTime = getJavaTime();
        mIsSimulated = true;
        return this;
    }

    public void pause() {
        mPauseStartTime = getTime();
        mIsPaused = true;
    }

    public void play() {
        if(mIsPaused) {
            mIsPaused = false;
            double timePaused = getTime() - mPauseStartTime;
            mStartTime += timePaused; // Offset clock by time paused

            System.out.println("Time elapsed: " + timePaused);
        }
    }

    private static double getJavaTime() {
        return System.currentTimeMillis() / 1000.0;
    }

    private static double getRobotTime() {
        return Timer.getFPGATimestamp();
    }

}
