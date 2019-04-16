package us.ilite.display.simulation;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public abstract class SimHarness {

    private static final ILog sLog = Logger.createLog(SimHarness.class);
    private final double mScheduleRate;

    private ScheduledExecutorService mModuleExecutor;
    private ScheduledFuture<?> mTask;
    private boolean mRunning = false;
    private Supplier<Boolean> mStopCondition = null;

    public SimHarness(double pScheduleRate) {
        mScheduleRate = pScheduleRate;
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
        long rateMs = (long)(mScheduleRate * 1000.0);
        sLog.warn("Initializing with DT: " + rateMs);

        init();

        mModuleExecutor = Executors.newScheduledThreadPool(1);
        mTask = mModuleExecutor.scheduleAtFixedRate(() -> periodic(), 0L, rateMs, TimeUnit.MILLISECONDS);

        return this;
    }

    public SimHarness stop() {
        mRunning = false;
        if(mTask != null) mTask.cancel(true);
        shutdown();
        return this;
    }

    public SimHarness setStopCondition(Supplier<Boolean> pStopCondition) {
        mStopCondition = pStopCondition;
        return this;
    }

    public boolean isRunning() {
        return mRunning;
    }

}
