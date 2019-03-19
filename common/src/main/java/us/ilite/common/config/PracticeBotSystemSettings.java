package us.ilite.common.config;

import us.ilite.common.lib.control.PIDGains;

public class PracticeBotSystemSettings {

    public static double kControlLoopPeriod = 0.01; // seconds

    public static double kNetworkTableUpdateRate = 0.01;

    public static PracticeBotSystemSettings getInstance() {
        return INSTANCE_HOLDER.sInstance;
    }

    private PracticeBotSystemSettings() {

    }

    private static class INSTANCE_HOLDER {
        private static final PracticeBotSystemSettings sInstance = new PracticeBotSystemSettings();
    }
}