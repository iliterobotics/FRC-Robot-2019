package us.ilite.robot.loops;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

import edu.wpi.first.wpilibj.Notifier;
import edu.wpi.first.wpilibj.Timer;
import us.ilite.lib.drivers.Clock;

/**
 * A class which uses the WPILIB Notifier mechanic to run our Modules on
 * a set time.  Tune loop period to the desired,
 * but monitor CPU usage.
 */
public class LoopManager implements Runnable{
    private ILog mLog = Logger.createLog(LoopManager.class);

    private final double kLoopPeriodSeconds;

    private final Notifier mWpiNotifier;
    private final Timer mLoopSafetyTimer;
    private final Clock mClock;

    private final LoopList mLoopList = new LoopList();

    private final Object mTaskLock = new Object();
    private boolean mIsRunning = false;

    public LoopManager(double pLoopPeriodSeconds) {
        mWpiNotifier = new Notifier(this);
        mLoopSafetyTimer = new Timer();
        mClock = new Clock();
        this.kLoopPeriodSeconds = pLoopPeriodSeconds;
    }

    public void setRunningLoops(Loop ... pLoops) {
        mLoopList.setLoops(pLoops);
    }

    public synchronized void start() {

        if(!mIsRunning) {
            mLog.info("Starting control loop");
            synchronized(mTaskLock) {
                mLoopList.modeInit(Timer.getFPGATimestamp());
                mLoopList.periodicInput(Timer.getFPGATimestamp());
                mIsRunning = true;
            }
            mWpiNotifier.startPeriodic(kLoopPeriodSeconds);
        }

        mClock.cycleEnded();

    }

    public synchronized void stop() {
        
        if(mIsRunning) {
            mLog.info("Stopping control loop");
            mWpiNotifier.stop();
            synchronized(mTaskLock) {
                mIsRunning = false;
                mLoopList.shutdown(Timer.getFPGATimestamp());
            }
        }

        mClock.cycleEnded();
    }

    @Override
    public void run() {
        synchronized(mTaskLock) {
            try {
                if(mIsRunning) {
                    mLoopList.periodicInput(Timer.getFPGATimestamp());
                    mLoopList.loop(Timer.getFPGATimestamp());
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        mClock.cycleEnded();
    }
    
}
