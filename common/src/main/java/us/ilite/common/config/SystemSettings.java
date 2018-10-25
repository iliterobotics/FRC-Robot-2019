package us.ilite.common.config;

public class SystemSettings {

    public static double    CONTROL_LOOP_PERIOD = 0.01;

    // =============================================================================
    // Drive Constants
    // =============================================================================
    public static double    DRIVE_RAMP_RATE = 0.1; // Seconds it should take for the talon to ramp from no power to full power
    public static int       DRIVE_CONTINUOUS_CURRENT_LIMIT_AMPS = 40;

    // =============================================================================
    // Motion Magic Constants
    // =============================================================================
    public static int		DRIVE_MOTION_MAGIC_PID_SLOT = 0;
    public static int		DRIVE_MOTION_MAGIC_LOOP_SLOT = 0;
    public static double	DRIVE_MOTION_MAGIC_P = 0.5;
    public static double	DRIVE_MOTION_MAGIC_I = 0.0;
    public static double	DRIVE_MOTION_MAGIC_D = 0.0;
    public static double	DRIVE_MOTION_MAGIC_F = 1023 / 951;
    public static int		DRIVE_MOTION_MAGIC_V = 951;
    public static int		DRIVE_MOTION_MAGIC_A = 951;

    // =============================================================================
    // Closed-Loop Position Constants
    // =============================================================================
    public static int       DRIVE_POSITION_TOLERANCE = 0;
    public static int       DRIVE_POSITION_PID_SLOT = 0;
    public static double    DRIVE_POSITION_P = 0.3;
    public static double    DRIVE_POSITION_I = 0;
    public static double    DRIVE_POSITION_D = 0;
    public static double    DRIVE_POSITION_F = 0;

    // Talon Configuration
    public static int       TALON_CONFIG_TIMEOUT_MS = 50; // How long we should wait for a error message before continuing
    public static int       TALON_DEFAULT_PID_IDX = 0;

}
