package us.ilite.robot.loops;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import us.ilite.robot.modules.Module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoopList extends Loop {

    ILog mLogger = Logger.createLog(LoopList.class);

    protected List<Loop> mLoops = new ArrayList<>();

    @Override
    public void powerOnInit(double pNow) {
        for(Module m : mLoops) {
            mLogger.info("Running power-on initialization for " + m.getClass().getSimpleName());
            m.powerOnInit(pNow);
        }
    }

    @Override
    public void modeInit(double pNow) {
        mLoops.forEach(module -> module.modeInit(pNow));
    }

    @Override
    public void update(double pNow) {
        mLoops.forEach(module -> module.update(pNow));
    }

    @Override
    public void shutdown(double pNow) {
        mLoops.forEach(module -> module.shutdown(pNow));
    }

    @Override
    public void checkModule(double pNow) {
        mLoops.forEach(module -> module.checkModule(pNow));
    }

    @Override
    public void loop(double pNow) {
        mLoops.forEach(loop -> loop.loop(pNow));
    }

    public void setLoops(Loop ... pLoops) {
        mLoops.clear();
        mLoops.addAll(Arrays.asList(pLoops));
    }

}
