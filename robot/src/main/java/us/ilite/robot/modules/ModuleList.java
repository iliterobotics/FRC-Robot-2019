package us.ilite.robot.modules;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModuleList extends Module {

    ILog mLogger = Logger.createLog(ModuleList.class);

    protected List<Module> mModules = new ArrayList<>();

    @Override
    public void modeInit(double pNow) {
        mModules.forEach(module -> module.modeInit(pNow));
    }

    @Override
    public void periodicInput(double pNow) {
        mModules.forEach(module -> module.periodicInput(pNow));
    }

    @Override
    public void update(double pNow) {
        mModules.forEach(module -> module.update(pNow));
    }

    @Override
    public void shutdown(double pNow) {
        mModules.forEach(module -> module.shutdown(pNow));
    }

    @Override
    public boolean checkModule(double pNow) {
        boolean allSuccessful = true;
            for (Module module : mModules) {
                boolean moduleSuccessful = module.checkModule(pNow);
            allSuccessful = allSuccessful && moduleSuccessful;
            if (!moduleSuccessful) {
                mLogger.error("Self-check failure for module: ", module.getClass());
            } else {
                mLogger.warn("Self-check success for module: ", module.getClass());
            }
        }

        return allSuccessful;
    }

    public void setModules(Module ... pModules) {
        mModules.clear();
        mModules.addAll(Arrays.asList(pModules));
    }

}
