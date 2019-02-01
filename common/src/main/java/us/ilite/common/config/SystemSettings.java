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

import us.ilite.common.lib.util.NetworkTablesConstantsBase;
import us.ilite.common.types.ETrackingType;
import us.ilite.common.types.input.ELogitech310;

public class SystemSettings extends NetworkTablesConstantsBase {


    public static double kControlLoopPeriod = 0.01; // seconds

    public static double NETWORK_TABLE_UPDATE_RATE = 0.01;

    //==============================================================================
    // Comms
    // =============================================================================
    public static SimpleNetworkTable AUTON_TABLE = new SimpleNetworkTable("AUTON_TABLE");
    public static SimpleNetworkTable kLoggingTable = new SimpleNetworkTable("LoggingTable");

    //==============================================================================
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
    public static double kSnailModePercentThrottleReduction = .5;
    public static double kSnailModePercentRotateReduction = .4;
    public static double  INPUT_DEADBAND_F310_JOYSTICK = 0.05;
    public static double  INPUT_DEADBAND_F310_TRIGGER = 0.5;
    public static int     JOYSTICK_PORT_DRIVER = 0;
    public static int     JOYSTICK_PORT_OPERATOR = 1;
    public static int     JOYSTICK_PORT_TESTER = 2;

    public static int kLimelightDefaultPipeline = ETrackingType.TARGET_LEFT.getPipeline();
    public static List<ELogitech310> kTeleopCommandTriggers = Arrays.asList(DriveTeamInputMap.DRIVER_TRACK_TARGET_BTN, 
                                                                            DriveTeamInputMap.DRIVER_TRACK_CARGO_BTN,
                                                                            DriveTeamInputMap.DRIVER_TRACK_HATCH_BTN);
    public static List<ELogitech310> kAutonOverrideTriggers = Arrays.asList(DriveTeamInputMap.DRIVER_THROTTLE_AXIS,
                                                                            DriveTeamInputMap.DRIVER_TURN_AXIS);
    public static double kAutonOverrideAxisThreshold = 0.3;

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
    public static final int ULTRASONIC_PORT = 2;
    
    // =============================================================================
    // Turn-To cPID constants
    // =============================================================================
    public static double kTurnP = 0.001;
    public static double kTurnI = 0.0;
    public static double kTurnD = 0.0;
    public static double kTurnF = 0.085;}

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
         * @return the heightfE
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
    // 2019 Module Addresses
    // =============================================================================
    public static int kCargoSpitSPXAddress = -1;
    public static double kCargoSpitSPXCurrentLimit = -1.0;

    public static int kElevatorNEOAddress = -1;
    public static int kElevatorNEOEncoderAddress = -1;
    // public static int kElevatorRedundantEncoderAddress = -1;

    public static int kFourBarNEO1Address = -1;
    public static int kFourBarNEO2Address = -1;
    public static int kFourBarDoubleSolenoidForwardAddress = -1;
    public static int kFourBarDoubleSolenoidReverseAddress = -1;
    public static int kFourBarTBDSensorAddress = -1;

    public static int kHatchFlowerOpenCloseSolenoidAddress = -1;
    public static int kHatchFlowerExtensionSolenoidAddress = -1;

    public static int kIntakeSPX1Address = -1;
    public static int kIntakeSPX2Address = -1;
    public static int kIntakeBeamBreakAddress = -1;

    public static int kIntakeWristSRXAddress = -1;
    public static int kIntakeWristEncoderA_Address = -1;
    public static int kIntakeWristEncoderB_Address = -1;
    public static double kIntakeWristCurrentLimit = -1.0;

    public static int kDriveTrainRightSRXAddress = -1;
    public static int kDriveTrainRightSPX1Address = -1;
    public static int kDriveTrainRightSPX2Address = -1;
    public static int kDriveTrainLeftSRXAddress = -1;
    public static int kDriveTrainLeftSPX1Address = -1;
    public static int kDriveTrainLeftSPX2Address = -1;






}
