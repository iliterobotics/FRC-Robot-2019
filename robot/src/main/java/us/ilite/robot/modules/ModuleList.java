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
    public void powerOnInit(double pNow) {
        for(Module m : mModules) {
            mLogger.info("Running power-on initialization for " + m.getClass().getSimpleName());
            m.powerOnInit(pNow);
        }
    }

    @Override
    public void modeInit(double pNow) {
        mModules.forEach(module -> module.modeInit(pNow));
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
    public void checkModule(double pNow) {
        mModules.forEach(module -> module.checkModule(pNow));
    }

    public void setModules(Module ... pModules) {
        mModules.clear();
        mModules.addAll(Arrays.asList(pModules));
    }

}
