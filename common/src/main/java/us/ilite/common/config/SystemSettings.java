package us.ilite.common.config;

import java.util.concurrent.TimeUnit;

import us.ilite.common.lib.util.ConstantsBase;

public class SystemSettings extends ConstantsBase {


    public static double kControlLoopPeriod = 0.01; // seconds
    public static TimeUnit SYSTEM_TIME_UNIT = TimeUnit.SECONDS;

    public static double NETWORK_TABLE_UPDATE_RATE = 0.01;

    //=============================================================================
    // Logging
    // =============================================================================
    public static String kLoggingTimestampKey = "TIME";

    // =============================================================================
    // Talon Addresses
    // =============================================================================
    // Encoders are on the rear Talons, so ID's are temporarily flipped around
    public static  int kDriveLeftMasterTalonId = 1;
    public static  int kDriveLeftRearTalonId = 3;
    public static  int kDriveRightMasterTalonId = 2;
    public static int kDriveRightRearTalonId = 4;
    
    //TODO Hypothetical elevator talons
    // public static int kElevatorMasterTalonId
    // public static int kElevatorFollowerTalonId

    public static int kPigeonId = 3;

    public static int kCANTimeoutMs = 10; //use for on the fly updates
    public static int kLongCANTimeoutMs = 100; //use for constructors

    // =============================================================================
    // Drive Train Constants
    // =============================================================================
    public static double kDriveClosedLoopVoltageRampRate = 0.0;
    public static double kDriveOpenLoopVoltageRampRate = 0.1;
    public static int kDriveCurrentLimitAmps = 80;
    public static double kDriveWheelDiameterInches = 6.0;
    public static double  DRIVETRAIN_WHEEL_DIAMETER_FEET = kDriveWheelDiameterInches / 12.0;
    public static double kDriveWheelCircumference = kDriveWheelDiameterInches * Math.PI;
    public static double  DRIVETRAIN_DEFAULT_RAMP_RATE = 120.0; // in V/sec
    public static double  DRIVETRAIN_HIGH_GEAR_RAMP_RATE = 120.0; // in V/sec
    public static double kDriveTicksPerRotation = 1024;
    public static double kDriveEffectiveWheelbase = 23.75 * 1.025;
    public static double 	DRIVETRAIN_TURN_CIRCUMFERENCE = kDriveEffectiveWheelbase * Math.PI;
    public static double	DRIVETRAIN_INCHES_PER_DEGREE = DRIVETRAIN_TURN_CIRCUMFERENCE / 360.0;
    public static double	DRIVETRAIN_WHEEL_TURNS_PER_DEGREE = DRIVETRAIN_INCHES_PER_DEGREE / kDriveWheelDiameterInches;

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
    // (Hypothetical) Elevator Constants
    // =============================================================================

    //TODO Actually change this please
    public static int kTopEncoderTicks = 0;

    //This is the value that it was last year. It will most likely change. 
    public static int kELEVATOR_ENCODER_DEADBAND = 20;

    // =============================================================================
    // Closed-Loop Velocity Constants
    // =============================================================================
    public static int kDriveVelocityTolerance = 0;
    public static int kDriveVelocityLoopSlot = 0;
    public static double kDriveVelocity_kP = 1.0;
    public static double kDriveVelocity_kI = 0.0;
    public static double kDriveVelocity_kD = 10.0;
//    public static double kDriveVelocity_kF = (1023.0 / 1155.0); // We don't care about this feedforward because we inject our own with ArbitraryFeedforward
    public static double kDriveVelocity_kF = 0.0; // We don't care about this feedforward because we inject our own with ArbitraryFeedforward
    @Override
    public String getFileLocation() {
        return "~/constants.txt";
    }

}
