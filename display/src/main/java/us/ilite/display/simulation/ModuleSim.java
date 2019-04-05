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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class ModuleSim {

    private static final ILog sLog = Logger.createLog(ModuleSim.class);
    private final double mScheduleRate;

    private final Timer mModuleExecutor = new Timer();
    private final Clock mClock;
    private final Data mData;
    private final ModuleList mModuleList = new ModuleList();

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
            sLog.warn("STOPPING MODULE EXECUTOR");
        }

        mModuleList.periodicInput(mClock.getCurrentTime());
        mModuleList.update(mClock.getCurrentTime());

//                mData.sendCodicesToNetworkTables();
        mClock.cycleEnded();
    }

    private void simShutdown() {
        mModuleList.shutdown(mClock.getCurrentTime());
    }

    public ModuleSim start() {
        long rate = (long)(mScheduleRate * 1000.0);
        sLog.warn("Initializing with DT: " + rate);

        simInit();

        mModuleExecutor.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                simPeriodic();
            }

        }, 0L, rate);

        return this;
    }

    public ModuleSim stop() {
        mModuleExecutor.cancel();
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

}
