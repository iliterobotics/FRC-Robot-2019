package us.ilite.robot.loops;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import edu.wpi.first.wpilibj.Notifier;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
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
    private Timer loopTimer = new Timer();
    private Timer updateTimer = new Timer();
    private Timer inputTimer = new Timer();
    private long numLoops = 0;
    private long numOverruns = 0;

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

            if(numLoops != 0) {
                mLog.error("Experienced ", numOverruns, "/", numLoops, " timing overruns, or ", ((double)numOverruns/(double)numLoops) * 100.0, "%.");
            }

            mClock.cycleEnded();
        }

    }

    @Override
    public void run() {
        if(mIsRunning) {
//            loopTimer.reset();
//            loopTimer.start();
            double start = Timer.getFPGATimestamp();
            synchronized (mTaskLock) {
                try {
                    if (mIsRunning) {
//                        inputTimer.reset();
//                        inputTimer.start();
                        mLoopList.periodicInput(Timer.getFPGATimestamp());
//                        inputTimer.stop();
//                        updateTimer.reset();
//                        updateTimer.start();
                        mLoopList.loop(Timer.getFPGATimestamp());
//                        updateTimer.stop();
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
//            mClock.cycleEnded();

//            Data.kSmartDashboard.getEntry("loop_input_dt").setDouble(inputTimer.get());
//            Data.kSmartDashboard.getEntry("loop_update_dt").setDouble(updateTimer.get());

//            loopTimer.stop();
            double dt = Timer.getFPGATimestamp() - start;
            numLoops++;
            SmartDashboard.putNumber("loop_dt", dt);
            if (dt > SystemSettings.kControlLoopPeriod) {
                mLog.error("Overrun: ", /*loopTimer.get()*/dt, " Input took: "/*, inputTimer.get()*/, " Update took: "/*,updateTimer.get()*/);
                numOverruns++;
            }
        }
    }
    
}
