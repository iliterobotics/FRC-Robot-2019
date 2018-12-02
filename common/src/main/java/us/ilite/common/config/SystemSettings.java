package us.ilite.common.config;

import us.ilite.common.lib.util.ConstantsBase;

import java.util.concurrent.TimeUnit;

public class SystemSettings extends ConstantsBase {


    public static double CONTROL_LOOP_PERIOD = 0.015; // seconds
    public static TimeUnit SYSTEM_TIME_UNIT = TimeUnit.SECONDS;

    public static double NETWORK_TABLE_UPDATE_RATE = 0.01;

    //=============================================================================
    // Logging
    // =============================================================================
    public static String LOGGING_TIMESTAMP_KEY = "TIME";

    // =============================================================================
    // Talon Addresses
    // =============================================================================
    public static  int kDriveLeftMasterTalonId = 10;
    public static  int kDriveLeftMiddleTalonId = 11;
    public static  int kDriveLeftRearTalonId = 12;
    public static  int kDriveRightMasterTalonId = 7; // Temporarily switch Master and Rear IDs
    public static  int kDriveRightMiddleTalonId = 6;
    public static  int kDriveRightRearTalonId = 5;

    public static int kPigeonId = 3;

    public static int kCANTimeoutMs = 10; //use for on the fly updates
    public static int kLongCANTimeoutMs = 100; //use for constructors

    // =============================================================================
    // Drive Train Constants
    // =============================================================================
    public static double kDriveClosedLoopVoltageRampRate = 0.0;
    public static double kDriveOpenLoopVoltageRampRate = 0.1;
    public static int kDriveCurrentLimitAmps = 40;
    public static double  DRIVETRAIN_WHEEL_DIAMETER = 5.875;
    public static double  DRIVETRAIN_WHEEL_DIAMETER_FEET = DRIVETRAIN_WHEEL_DIAMETER / 12.0;
    public static double  DRIVETRAIN_WHEEL_CIRCUMFERENCE = DRIVETRAIN_WHEEL_DIAMETER * Math.PI;
    public static double  DRIVETRAIN_DEFAULT_RAMP_RATE = 120.0; // in V/sec
    public static double  DRIVETRAIN_HIGH_GEAR_RAMP_RATE = 120.0; // in V/sec
    public static double  DRIVETRAIN_ENC_TICKS_PER_TURN = 1024;
    public static double	DRIVETRAIN_EFFECTIVE_WHEELBASE = 25.5;
    public static double 	DRIVETRAIN_TURN_CIRCUMFERENCE = DRIVETRAIN_EFFECTIVE_WHEELBASE * Math.PI;
    public static double	DRIVETRAIN_INCHES_PER_DEGREE = DRIVETRAIN_TURN_CIRCUMFERENCE / 360;
    public static double	DRIVETRAIN_WHEEL_TURNS_PER_DEGREE = DRIVETRAIN_INCHES_PER_DEGREE / DRIVETRAIN_WHEEL_DIAMETER;

    // =============================================================================
    // Input Constants
    // =============================================================================
    public static double  SNAIL_MODE_THROTTLE_LIMITER = .5;
    public static double  SNAIL_MODE_ROTATE_LIMITER = .4;
    public static double  INPUT_DEADBAND_F310_JOYSTICK = 0.05;
    public static double  INPUT_DEADBAND_F310_TRIGGER = 0.5;
    public static int     JOYSTICK_PORT_DRIVER = 0;
    public static int     JOYSTICK_PORT_OPERATOR = 1;
    public static int     JOYSTICK_PORT_TESTER = 2;

    // =============================================================================
    // Motion Magic Constants
    // =============================================================================
    public static int kDriveMotionMagicLoopSlot = 0;
    public static int kDriveMotionMagicVelocityFeedforward = 0;
    public static int kDriveMotionMagicAccelFeedforward = 0;

    // =============================================================================
    // Closed-Loop Position Constants
    // =============================================================================
    public static int kDrivePositionTolerance = 0;
    public static int kDrivePositionLoopSlot = 1;
    public static double kDrivePosition_kP = 0;
    public static double kDrivePosition_kI = 0;
    public static double kDrivePosition_kD = 0;
    public static double kDrivePosition_kF = 0;

    // =============================================================================
    // Closed-Loop Velocity Constants
    // =============================================================================
    public static int kDriveVelocityTolerance = 0;
    public static int kDriveVelocityLoopSlot = 0;
    public static double kDriveVelocity_kP = 0.0;
    public static double kDriveVelocity_kI = 0.0;
    public static double kDriveVelocity_kD = 0.0;
    public static double kDriveVelocity_kF = 0.0; // We don't care about this feedforward because we inject our own with ArbitraryFeedforward

    @Override
    public String getFileLocation() {
        return "~/constants.txt";
    }

}
