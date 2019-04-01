package us.ilite.display.simulation;

import us.ilite.lib.drivers.Clock;
import us.ilite.robot.modules.Module;
import us.ilite.robot.modules.ModuleList;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class ModuleSim {

    private final double mScheduleRate;

    private final ScheduledExecutorService mModuleExecutor = Executors.newSingleThreadScheduledExecutor();
    private final Clock mClock = new Clock().simulated();
    private final ModuleList mModuleList = new ModuleList();

    private Supplier<Boolean> mStopCondition = null;

    public ModuleSim(double pScheduleRate, Module ... pModules) {
        mScheduleRate = pScheduleRate;
        setModules(pModules);
    }

    public ModuleSim start() {
        mModuleList.modeInit(mClock.getCurrentTime());
        mModuleExecutor.scheduleAtFixedRate(() -> {

            if(mStopCondition != null && mStopCondition.get()) {
                stop();
            }

            mModuleList.update(mClock.getCurrentTime());
            mClock.cycleEnded();
        }, 0L, (long)mScheduleRate, TimeUnit.SECONDS);
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
