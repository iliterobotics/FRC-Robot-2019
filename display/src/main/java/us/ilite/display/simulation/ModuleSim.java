package us.ilite.display.simulation;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import us.ilite.lib.drivers.Clock;
import us.ilite.robot.modules.Module;
import us.ilite.robot.modules.ModuleList;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class ModuleSim {

    private static final ILog sLog = Logger.createLog(ModuleSim.class);
    private final double mScheduleRate;

    private final ScheduledExecutorService mModuleExecutor = Executors.newScheduledThreadPool(1);
    private final Clock mClock = new Clock().simulated();
    private final ModuleList mModuleList = new ModuleList();

    private Supplier<Boolean> mStopCondition = null;

    public ModuleSim(double pScheduleRate, Module ... pModules) {
        mScheduleRate = pScheduleRate;
        setModules(pModules);
    }

    public ModuleSim start() {
        long rate = (long)(mScheduleRate * 1000.0);
        sLog.debug("Initializing with DT: " + rate);
        mModuleList.modeInit(mClock.getCurrentTime());
        mModuleExecutor.scheduleAtFixedRate(() -> {

            sLog.debug("Updating: " + mClock.getCurrentTime());
            if(mStopCondition != null && mStopCondition.get()) {
                stop();
                sLog.debug("STOPPING MODULE EXECUTOR");
            }

            mModuleList.periodicInput(mClock.getCurrentTime());
            mModuleList.update(mClock.getCurrentTime());
            mClock.cycleEnded();

        }, 1L, rate, TimeUnit.MILLISECONDS);
        return this;
    }

    public ModuleSim stop() {
        mModuleExecutor.shutdown();
        mModuleList.shutdown(mClock.getCurrentTime());
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
