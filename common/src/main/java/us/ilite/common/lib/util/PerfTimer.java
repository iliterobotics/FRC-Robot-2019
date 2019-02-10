package us.ilite.common.lib.util;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

public class PerfTimer {

    private final ILog mLog = Logger.createLog(PerfTimer.class);

    private double mStartTime = Double.NaN;
    private double mWarningThreshold = Double.NaN;
    private String mWarningMessage = "Time warning: %s";

    public void start() {
        mStartTime = getSeconds();
    }

    public double stop() {
        double delta = getSeconds() - mStartTime;

        if(mWarningThreshold != Double.NaN && mWarningThreshold <= delta) {
            mLog.warn(String.format(mWarningMessage, delta));
        }

        return delta;
    }

    public PerfTimer logWhenTimeGreaterThan(double pTime) {
        mWarningThreshold = pTime;
        return this;
    }

    public PerfTimer alwayLog() {
        mWarningThreshold = 0.0;
        return this;
    }

    public PerfTimer setLogMessage(String pMessage) {
        mWarningMessage = pMessage;
        return this;
    }

    private double getSeconds() {
        return System.currentTimeMillis() / 1000.0;
    }
}
