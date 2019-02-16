package us.ilite.common.config;

import java.util.Arrays;
import java.util.List;

import us.ilite.common.lib.control.PIDGains;
import us.ilite.common.types.ETrackingType;
import us.ilite.common.types.input.ELogitech310;

public class PracticeBotSystemSettings {

    public static double kControlLoopPeriod = 0.01; // seconds

    public static double NETWORK_TABLE_UPDATE_RATE = 0.01;


    // =============================================================================
    // Drive Train Constants
    // =============================================================================
    public static double kDriveWheelDiameterInches = 6.0;
    public static double  DRIVETRAIN_WHEEL_DIAMETER_FEET = kDriveWheelDiameterInches / 12.0;
    public static double kDriveWheelCircumference = kDriveWheelDiameterInches * Math.PI;
    public static double kDriveTicksPerRotation = 1024;
    public static double kDriveEffectiveWheelbase = 23.75 * 1.025;
    public static double 	DRIVETRAIN_TURN_CIRCUMFERENCE = kDriveEffectiveWheelbase * Math.PI;
    public static double	DRIVETRAIN_INCHES_PER_DEGREE = DRIVETRAIN_TURN_CIRCUMFERENCE / 360.0;
    public static double	DRIVETRAIN_WHEEL_TURNS_PER_DEGREE = DRIVETRAIN_INCHES_PER_DEGREE / kDriveWheelDiameterInches;
    public static double kDriveCollisionThreshold = 0.0;


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
    // Elevator Constants
    // =============================================================================

    //All these values will be changed once we have a better idea of what the
    //elevator's properties will be like
    public static int kTopEncoderTicks = 0;

    public static double kElevatorP = 0.1;
    public static double kElevatorI = 0; 
    public static double kElevatorD = 0;
    public static double kElevatorF = 0;
    public static double kElevatorMinPower = -1.0;
    public static double kElevatorMaxPower = 1.0;
    public static int kElevatorCurrentLimit = 10;
    public static int kELEVATOR_ENCODER_DEADBAND = 20;

    // =============================================================================
    // Closed-Loop Velocity Constants
    // =============================================================================
    public static int kDriveVelocityTolerance = 0;
    public static int kDriveVelocityLoopSlot = 0;
    public static double kDriveVelocity_kP = 0.5;
    public static double kDriveVelocity_kI = 0.0;
    public static double kDriveVelocity_kD = 0.0;
    public static double kDriveVelocity_kF = 0.0; // We don't care about this feedforward because we inject our own with ArbitraryFeedforward
    public static int ULTRASONIC_PORT = 2;

    // =============================================================================
    // Turn-To PID constants
    // =============================================================================
    public static PIDGains kPIDGains = new PIDGains( 0.0, 0.0, 0.0, 0.085 );

    // =============================================================================
    // LimeLight Camera Constants
    // Note: These constants need to be recalculted for a specific robot geometry
    // =============================================================================
    public static double llCameraHeightIn = 58.0;
    public static double llCameraToBumperIn = 10.0;
    public static double llCameraAngleDeg = 28.55;

    // Left angle coefficients for angle = a + bx + cx^2
    //    a	0.856905324060421
    //    b	-3.01414088331715
    //    c	-0.0331854848038372
    public static double llLeftACoeff = 0.856905324060421;
    public static double llLeftBCoeff = -3.01414088331715;
    public static double llLeftCCoeff = -0.0331854848038372;

    // Right angle coefficients for angle = a + bx + cx^2
    // a	-54.3943883842204
    // b	-4.53956454545558
    // c	-0.0437470770400814
    public static double llRightACoeff = -54.3943883842204;
    public static double llRightBCoeff = -4.53956454545558;
    public static double llRightCCoeff = -0.0437470770400814;

    public static PracticeBotSystemSettings getInstance() {
        return INSTANCE_HOLDER.sInstance;
    }
    private PracticeBotSystemSettings() {

    }
    private static class INSTANCE_HOLDER {
        private static final PracticeBotSystemSettings sInstance = new PracticeBotSystemSettings();
    }
}