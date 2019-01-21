package us.ilite.common.config;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.team254.lib.util.ConstantsBase;

import us.ilite.common.lib.util.NetworkTablesConstantsBase;
import us.ilite.common.types.ETrackingType;
import us.ilite.common.types.input.ELogitech310;

public class SystemSettings extends NetworkTablesConstantsBase {


    public static double kControlLoopPeriod = 0.01; // seconds

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
    public static  int kDriveRightRearTalonId = 4;

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

    public static int kLimelightDefaultPipeline = ETrackingType.TARGET_TRACK.getRightPipelineNum();
    public static List<ELogitech310> kTeleopCommandTriggers = Arrays.asList(DriveTeamInputMap.DRIVER_TRACK_TARGET_BTN, 
                                                                            DriveTeamInputMap.DRIVER_TRACK_CARGO_BTN,
                                                                            DriveTeamInputMap.DRIVER_TRACK_HATCH_BTN);
    

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
    public static double kDriveVelocity_kP = 1.0;
    public static double kDriveVelocity_kI = 0.0;
    public static double kDriveVelocity_kD = 10.0;
//    public static double kDriveVelocity_kF = (1023.0 / 1155.0); // We don't care about this feedforward because we inject our own with ArbitraryFeedforward
    public static double kDriveVelocity_kF = 0.0; // We don't care about this feedforward because we inject our own with ArbitraryFeedforward

    // =============================================================================
    // Turn-To cPID constants
    // =============================================================================
    public static double kTurnP = 0.001;
    public static double kTurnI = 0.0;
    public static double kTurnD = 0.0;
    public static double kTurnF = 0.085;

    public static int ULTRASONIC_PORT = 2;
}
