package us.ilite.robot.loops;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import edu.wpi.first.wpilibj.Notifier;
import edu.wpi.first.wpilibj.Timer;
import us.ilite.common.config.SystemSettings;
import us.ilite.lib.drivers.Clock;
import us.ilite.robot.Data;

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
    Timer loopTimer = new Timer();
    Timer updateTimer = new Timer();
    Timer inputTimer = new Timer();

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
        if(mIsRunning) {
            loopTimer.reset();
            loopTimer.start();
            synchronized (mTaskLock) {
                try {
                    if (mIsRunning) {
                        inputTimer.reset();
                        inputTimer.start();
                        mLoopList.periodicInput(Timer.getFPGATimestamp());
                        inputTimer.stop();
                        updateTimer.reset();
                        updateTimer.start();
                        mLoopList.loop(Timer.getFPGATimestamp());
                        updateTimer.stop();
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            mClock.cycleEnded();
            Data.kSmartDashboard.putDouble("loop_dt", loopTimer.get());
            if (loopTimer.get() > SystemSettings.kControlLoopPeriod) {
                mLog.error("Overrun: ", loopTimer.get(), " Input took: ", inputTimer.get(), " Update took: ", updateTimer.get());
            }
        }
    }
    
}
