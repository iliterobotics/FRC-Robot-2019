package us.ilite.robot.loops;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoopList extends Loop {

    ILog mLogger = Logger.createLog(LoopList.class);

    protected List<Loop> mLoops = new ArrayList<>();

    @Override
    public void modeInit(double pNow) {
        mLoops.forEach(module -> module.modeInit(pNow));
    }

    @Override
    public void periodicInput(double pNow) {
        mLoops.forEach(module -> module.periodicInput(pNow));
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
    public boolean checkModule(double pNow) {
        boolean allSuccessful = true;
        for (Loop loop : mLoops) {
            boolean moduleSuccessful = loop.checkModule(pNow);
            allSuccessful = allSuccessful && moduleSuccessful;
            if (!moduleSuccessful) {
                mLogger.error("Self-check failure for module: ", loop.getClass());
            } else {
                mLogger.warn("Self-check success for module: ", loop.getClass());
            }
        }

        return allSuccessful;
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
