package us.ilite.common.config;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import us.ilite.common.types.auton.EHatchAction;
import us.ilite.common.types.auton.ECargoAction;
import us.ilite.common.types.auton.EStartingPosition;
import us.ilite.common.lib.util.SimpleNetworkTable;

import com.team254.lib.util.ConstantsBase;

import us.ilite.common.lib.control.PIDGains;
import us.ilite.common.lib.util.NetworkTablesConstantsBase;
import us.ilite.common.types.ETrackingType;
import us.ilite.common.types.input.ELogitech310;

public class SystemSettings extends NetworkTablesConstantsBase {


    public static double kControlLoopPeriod = 0.01; // seconds

    public static double NETWORK_TABLE_UPDATE_RATE = 0.01;

    public static final int sCODEX_COMMS_PORT = 5805;

    //==============================================================================
    // Comms
    // =============================================================================
    public static SimpleNetworkTable AUTON_TABLE = new SimpleNetworkTable("AUTON_TABLE");
    public static SimpleNetworkTable kLoggingTable = new SimpleNetworkTable("LoggingTable");

    // ===========================
    // System ID's
    // ================================
    public static int kCANAddressPCM = 20;

    //==============================================================================
    // Logging
    // =============================================================================
    public static String kLoggingTimestampKey = "TIME";

    // =============================================================================
    // Talon Addresses
    // =============================================================================
    // Encoders are on the rear Talons, so ID's are temporarily flipped around
    public static  int kDriveLeftMasterTalonId = 1;
    public static int kDriveLeftMiddleTalonId = 3;
    public static  int kDriveLeftRearTalonId = 5;
    public static  int kDriveRightMasterTalonId = 2;
    public static int kDriveRightMiddleTalonId = 4;
    public static  int kDriveRightRearTalonId = 6;

    public static int kPigeonId = 3;

    public static int kCANTimeoutMs = 10; //use for on the fly updates
    public static int kLongCANTimeoutMs = 100; //use for constructors

    // =============================================================================
    // Drive Train Constants
    // =============================================================================
    public static double kDriveClosedLoopVoltageRampRate = 0.0;
    public static double kDriveOpenLoopVoltageRampRate = 0.1;
    public static int kDriveCurrentLimitAmps = 40;
    public static int kDriveCurrentLimitTriggerDurationMs = 100;
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
    // IMU Constants
    // =============================================================================
    public static double kGyroCollisionThreshold = 0.0;
    public static int kGyroUpdateRate = 200;

    // =============================================================================
    // Heading Gains
    // =============================================================================
    public static PIDGains kDriveHeadingGains = new PIDGains(0.03, 0.0, 0.0);
    public static double kDriveLinearPercentOutputLimit = 0.5;

    // =============================================================================
    // Input Constants
    // =============================================================================
    public static double kSnailModePercentThrottleReduction = .5;
    public static double kSnailModePercentRotateReduction = .4;
    public static double kDriverInputTurnMaxMagnitude = 0.5;
    public static double  INPUT_DEADBAND_F310_JOYSTICK = 0.05;
    public static double  INPUT_DEADBAND_F310_TRIGGER = 0.5;
    public static int     JOYSTICK_PORT_DRIVER = 0;
    public static int     JOYSTICK_PORT_OPERATOR = 1;
    public static int     JOYSTICK_PORT_TESTER = 2;

    public static int kLimelightDefaultPipeline = ETrackingType.TARGET_LEFT.getPipeline();
    public static List<ELogitech310> kTeleopCommandTriggers = Arrays.asList(DriveTeamInputMap.DRIVER_TRACK_TARGET_BTN, 
                                                                            DriveTeamInputMap.DRIVER_TRACK_CARGO_BTN,
                                                                            DriveTeamInputMap.DRIVER_TRACK_HATCH_BTN,
                                                                            DriveTeamInputMap.OPERATOR_ARM_MOTION);
    public static List<ELogitech310> kAutonOverrideTriggers = Arrays.asList(DriveTeamInputMap.DRIVER_THROTTLE_AXIS,
                                                                            DriveTeamInputMap.DRIVER_TURN_AXIS);
    public static double kAutonOverrideAxisThreshold = 0.3;

    // =============================================================================
    // Motion Magic Constants
    // =============================================================================
    public static int kDriveMotionMagicLoopSlot = 0;
    public static int kDriveMotionMagicCruiseVelocity = 0;
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
    public static double kELevatorControlLoopPeriod = 0.01;
    // public static int kUpperElevatorEncoderThreshold = 0; //Will be calculated on the regular
    // public static int kLowerElevatorEncoderThreshold = 0;
    public static double kElevatorMinPower = -1.0;
    public static double kElevatorMaxPower = 1.0;
    public static int kElevatorCurrentLimit = 10;

    // public static int kCansparkMasterId = 0;
    // public static int kTalonId = 0;

    //This is the value that it was last year. It will most likely change. 
    public static int kELEVATOR_ENCODER_DEADBAND = 20;

    // =============================================================================
    // Closed-Loop Velocity Constants
    // =============================================================================
    public static int kDriveVelocityTolerance = 0;
    public static int kDriveVelocityLoopSlot = 0;
    public static double kDriveVelocity_kP = 1.0;
    public static double kDriveVelocity_kI = 0.0;
    public static double kDriveVelocity_kD = 0.0;
//    public static double kDriveVelocity_kF = (1023.0 / 1155.0); // We don't care about this feedforward because we inject our own with ArbitraryFeedforward
    public static double kDriveVelocity_kF = 0.0; // We don't care about this feedforward because we inject our own with ArbitraryFeedforward
    public static int ULTRASONIC_PORT = 2;

    // =============================================================================
    // Turn-To PID constants
    // =============================================================================
    public static PIDGains kPIDGains = new PIDGains( 0.0, 0.0, 0.0, 0.085 );



    // =============================================================================
    // Arm Constants
    // =============================================================================

    // Predefined arm  positions
    public enum ArmPosition {
        FULLY_OUT(90.0),
        FULLY_UP(135.0),
        FULLY_DOWN(0.0);

        private final double angle;

        ArmPosition( double angle ) {
            this.angle = angle;
        }

        public double getAngle() {
            return this.angle;
        }
    }

    public static int kArmPositionEncoderTicksPerRotation = 3552;
    public static double kArmMinAngle = 0.0;
    public static double kArmMaxAngle = 135.0;
    public static double kArmMaxCurrentVoltRatio = 2; //tune - overcurrent ratio for arm motor
    public static double kArmMotorOffTimeSec = 0.5; // seconds
    public static double kArmMaxStallTimeSec = 0.1; // seconds
    public static double kArmMinMotorStallVoltage = 0.1;


    //////// BasicArm Constants /////////
    // TODO We may need to increase kD for stability
    // PID Gains                                        kP,    kI,    kD
    public static PIDGains kArmPIDGains = new PIDGains( 0.01, 0.000, 0.0008 );
    // Dampen as we get close to our target
    public static PIDGains kArmLandingPIDGains = new PIDGains( 0.02, 0.003, 0.001 );
    // public static PIDGains kArmLandingPIDGains = kArmPIDGains;
    // Range +/- of target angle where we apply the landing PID Gains
    public static double kArmLandingRangeAngle = 10.0;

    // We measured .7 volts on the motor to hold the arm horizontal
    // For gravity compensation Kg = % power to hold arm horizontal, which is
    // the measured voltage / 12 volts
    public static double kArmKg = 0.7/12.0;

    // Control power clamping limits
    public static double kArmPIDOutputMaxLimit = 1.0; // max 1.0
    public static double kArmPIDOutputMinLimit = -1.0; // min -1.0
    /////////////////////////////////////


    ///// MotionMagicArm Constants //////
    // PID Gains
    public static double kArmPidP = 0.1;
    public static double kArmPidI = 0.020;
    public static double kArmPidD = 0.0;
    public static double kArmPidF = 0.1;
    // public static int K_ARM_ACCELERATION = 512;
    // public static int K_ARM_CRUISE = 4096;
    public static int K_ARM_ACCELERATION = 5;
    public static int K_ARM_CRUISE = 30;
    /////////////////////////////////////

    public static int CTRE_TIMEOUT_INIT = 10;
    public static int CTRE_TIMEOUT_PERIODIC = 0;
    // =============================================================================




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



    // =============================================================================
    // Target Constants
    // Note: These constants need to be recalculted for the specific target geometry
    // =============================================================================
    // TODO These values are specific to the targets, not the camera, and may belong elsewhere
    // The current target values assume the limelight processing stream is configured to target
    // the bottom of the vision target
    public enum VisionTarget {
        HatchPort(25.6875), // height of the bottom of the reflective tape in inches for the hatch port
        CargoPort(33.3125), // height of the bottom of the reflective tape in inches for the cargo port
        Ground(0.0,"Ground_Tape_Tracking.vpr"), //The ground
        CargoHeight(6.5d,"Cargo_Ball_Tracking.vpr");//This may change, not sure what the correct value

        private final double height;
        private final Optional<String> pipelineName;

        VisionTarget(double height) {
            this(height, null);
        }
        VisionTarget( double height, String pipelineName)  {
            this.height = height;
            this.pipelineName = Optional.ofNullable(pipelineName);
        }

        /**
         * @return the height
         */
        public double getHeight() {
            return height;
        }
        /**
         * @return the pipelineName
         */
        public Optional<String> getPipelineName() {
            return pipelineName;
        }

    }

    // =============================================================================
    // Hatch Flower constants
    // =============================================================================
    public static double kHatchFlowerSolenoidReleaseTimeSec = 0.250;
    public static double kHatchFlowerPushDurationSec = 0.250;

    // kHatchFlowerGrabToPushTransitionTimeSec is the time between releasing the 
    // grab solenoid and engaging the push solenoid.
    public static double kHatchFlowerGrabToPushTransitionTimeSec = 0.250;

    public static double kHatchFlowerExtendStatusTimerDuration = 1.0;
    public static double kHatchFlowerPushStatusTimerDuration = 1.0;


    // =============================================================================
    // 2019 Module Addresses
    // =============================================================================
    public static int kCargoSpitLeftSPXAddress = 13;
    public static int kCargoSpitRightSPXAddress = 14;
    public static double kCargoSpitSPXCurrentLimit = -1.0;

    public static int kElevatorNEOAddress = -15;
    // TO-DO: Elevator encoder address?
    public static int kElevatorNEOEncoderAddress = -1;
    // public static int kElevatorRedundantEncoderAddress = -1;

    public static int kFourBarNEO1Address = 9;
    public static int kFourBarNEO2Address = 10;
    // TO-DO: label solenoid as forward/reverse in spreadsheet
    public static int kFourBarDoubleSolenoidForwardAddress = 0;
    public static int kFourBarDoubleSolenoidReverseAddress = 1;
    public static int kFourBarTBDSensorAddress = -1;

    public static int kHatchFlowerOpenCloseSolenoidAddress = 5;
    public static int kHatchFlowerExtensionSolenoidAddress = 6;
    public static int kHatchFlowerPushSolenoidAddress = 7;

    public static int kHatchIntakeSPXAddress = 11;
    public static int kCargoIntakeSPXLowerAddress = 12;
    // TO-DO DIO spreadsheet empty
    // public static int kIntakeBeamBreakAddress = -1;

    public static int kIntakeWristSRXAddress = 16;
    // TO-DO Writs encoder addresses?
    public static int kIntakeWristEncoderA_Address = -1;
    public static int kIntakeWristEncoderB_Address = -1;
    public static int kIntakeSolenoidAddress = 2; // and/or 3 according to integration sheet
    public static double kIntakeRollerHatchPower = -1;
    public static double kIntakeRollerCargoPower = -1;
    public static double kIntakeRollerHoldPower = -1;
    public static double kIntakeWristStowedAngle = -1;
    public static double kIntakeWristHandoffAngle = -1;
    public static double kIntakeWristGroundAngle = -1;
    //The minimum angle where it is safe to continue intake process (engage solenoid/roller)
    public static double kIntakeWristGroundMinBound = -1;

    public static int kDriveTrainRightSRX1Address = 2;
    public static int kDriveTrainRightSPX2Address = 4;
    public static int kDriveTrainRightSPX3Address = 6;
    public static int kDriveTrainLeftSRX1Address = 1;
    public static int kDriveTrainLeftSPX2Address = 3;
    public static int kDriveTrainLeftSPX3Address = 5;

    public static int kArmTalonSRXAddress = 6;





}
