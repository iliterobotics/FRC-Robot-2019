package us.ilite.display.simulation;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import us.ilite.lib.drivers.Clock;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public abstract class SimHarness {

    private static final ILog sLog = Logger.createLog(SimHarness.class);
    private final double mScheduleRate;

    private ScheduledExecutorService mModuleExecutor = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> mTask;
    private boolean mRunning = false;
    private Supplier<Boolean> mStopCondition = null;

    private Clock mClock = null;

    public SimHarness(double pMScheduleRate) {
        mScheduleRate = pMScheduleRate;
    }

    protected abstract void simInit();
    protected abstract void simPeriodic();
    protected abstract void simShutdown();

    private void init() {
        simInit();
    }

    private void periodic() {
        if(mStopCondition.get()) {
            stop();
        }

        simPeriodic();
    }

    private void shutdown() {
        simShutdown();
    }

    public SimHarness start() {
        mRunning = true;

        init();

        mTask = startModuleExecutor();

        return this;
    }

    public SimHarness stop() {
        mRunning = false;
        if(mTask != null) mTask.cancel(true);
        shutdown();
        return this;
    }

    public void suspend() {
        if(mTask != null) mTask.cancel(true);
        if(mClock != null) mClock.pause();
    }

    public void resume() {
        mTask = startModuleExecutor();
        if(mClock != null) mClock.play();
    }

    public SimHarness setStopCondition(Supplier<Boolean> pStopCondition) {
        mStopCondition = pStopCondition;
        return this;
    }

    public SimHarness setClock(Clock pClock) {
        mClock = pClock;
        return this;
    }

    private ScheduledFuture<?> startModuleExecutor() {
        long rateMs = (long)(mScheduleRate * 1000.0);
        sLog.warn("Initializing with DT: " + rateMs);

        return  mModuleExecutor.scheduleAtFixedRate(this::periodic, 0L, rateMs, TimeUnit.MILLISECONDS);
    }

    public double getTime() {
        return (mClock == null) ? 0.0 : mClock.getCurrentTime();
    }

    public boolean isRunning() {
        return mRunning;
    }

}
