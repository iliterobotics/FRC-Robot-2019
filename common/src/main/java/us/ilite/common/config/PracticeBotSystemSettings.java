package us.ilite.common.config;

import us.ilite.common.lib.control.PIDGains;

public class PracticeBotSystemSettings {

    public static double kControlLoopPeriod = 0.01; // seconds

    public static double kNetworkTableUpdateRate = 0.01;


    // =============================================================================
    // Drive Train Constants
    // =============================================================================
    public static double kDriveGearboxRatio = (12.0 / 80.0) * (42.0 / 80.0);
    public static double kDriveWheelDiameterInches = 6.0;
    public static double kDriveWheelCircumference = kDriveWheelDiameterInches * Math.PI;
    public static double kDriveTicksPerRotation = 1024.0;
    public static double kDriveEffectiveWheelbase = 23.25;

    public static double kTargetAngleLockFrictionFeedforward = 0.055;
    public static PIDGains kTargetAngleLockGains = new PIDGains(0.00055, 0.000, 0.0);


    public static PracticeBotSystemSettings getInstance() {
        return INSTANCE_HOLDER.sInstance;
    }

    private PracticeBotSystemSettings() {

    }

    private static class INSTANCE_HOLDER {
        private static final PracticeBotSystemSettings sInstance = new PracticeBotSystemSettings();
    }
}