package us.ilite.display.simulation;

import com.flybotix.hfr.codex.CodexMetadata;
import com.flybotix.hfr.codex.ICodexTimeProvider;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import us.ilite.common.Data;
import us.ilite.lib.drivers.Clock;
import us.ilite.robot.modules.Module;
import us.ilite.robot.modules.ModuleList;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class ModuleSim {

    private static final ILog sLog = Logger.createLog(ModuleSim.class);
    private final double mScheduleRate;

    private final Clock mClock;
    private final Data mData;
    private final ModuleList mModuleList = new ModuleList();

    private ScheduledExecutorService mModuleExecutor;
    private ScheduledFuture<?> mTask;
    private boolean mRunning = false;
    private Supplier<Boolean> mStopCondition = null;

    public ModuleSim(double pScheduleRate, Clock pClock, Data pData, Module ... pModules) {
        mScheduleRate = pScheduleRate;
        mClock = pClock;
        mData = pData;
        setModules(pModules);
    }

    private void simInit() {
        ICodexTimeProvider provider = new ICodexTimeProvider() {
            public long getTimestamp() {
                return (long) mClock.getCurrentTimeInNanos();
            }
        };
        CodexMetadata.overrideTimeProvider(provider);
        mData.registerCodices();
        mModuleList.modeInit(mClock.getCurrentTime());
    }

    private void simPeriodic() {
        if (mStopCondition != null && mStopCondition.get()) {
            stop();
            sLog.error("STOPPING MODULE EXECUTOR");
        }
        mModuleList.periodicInput(mClock.getCurrentTime());
        mModuleList.update(mClock.getCurrentTime());
        mData.sendCodices();
//      mData.sendCodicesToNetworkTables();
        mClock.cycleEnded();
    }

    private void simShutdown() {
        mModuleList.shutdown(mClock.getCurrentTime());
    }

    public ModuleSim start() {
        mRunning = true;
        long rateMs = (long)(mScheduleRate * 1000.0);
        sLog.warn("Initializing with DT: " + rateMs);

        simInit();

        mModuleExecutor = Executors.newScheduledThreadPool(1);
        mTask = mModuleExecutor.scheduleAtFixedRate(() -> simPeriodic(), 0L, rateMs, TimeUnit.MILLISECONDS);

        return this;
    }

    public ModuleSim stop() {
        mRunning = false;
        if(mTask != null) mTask.cancel(true);
        simShutdown();
        return this;
    }

    public ModuleSim setModules(Module ... pModules) {
        mModuleList.setModules(pModules);
        return this;
    }

    public ModuleSim setStopCondition(Supplier<Boolean> pStopCondition) {
        mStopCondition = pStopCondition;
        return this;
    }

    public boolean isRunning() {
        return mRunning;
    }

}
