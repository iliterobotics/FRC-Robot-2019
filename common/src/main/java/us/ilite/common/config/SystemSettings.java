package us.ilite.common.config;

import java.util.Arrays;
import java.util.List;

import com.team254.lib.util.CheesyDriveGains;

import us.ilite.common.lib.control.PIDGains;
import us.ilite.common.lib.util.NetworkTablesConstantsBase;
import us.ilite.common.types.ETrackingType;
import us.ilite.common.types.input.ELogitech310;
import us.ilite.common.types.sensor.EPowerDistPanel;

public class SystemSettings extends NetworkTablesConstantsBase {

    public static double kControlLoopPeriod = 0.01; // seconds
    public static double kCSVLoggingPeriod = 0.02;  // seconds

    public static double kNetworkTableUpdateRate = 0.01;

    public static int sCODEX_COMMS_PORT = 5805;

    // ===========================
    // System ID's
    // ================================
    public static int kCANAddressPCM = 20;

    //==============================================================================
    // Logging
    // =============================================================================

    public static int kCANTimeoutMs = 10; //use for on the fly updates
    public static int kLongCANTimeoutMs = 100; //use for constructors

    // =============================================================================
    // Drive Train Constants
    // =============================================================================
    public static double kDriveGearboxRatio = (12.0 / 80.0) * (42.0 / 80.0);
    public static double kDriveWheelDiameterInches = 6.0;
    public static double kDriveWheelCircumference = kDriveWheelDiameterInches * Math.PI;
    public static double kDriveTicksPerRotation = 1.0;
    public static double kDriveEffectiveWheelbase = 23.25;

    public static double kDriveClosedLoopVoltageRampRate = 0.0;
    public static double kDriveMinOpenLoopVoltageRampRate = 0.1;
    public static double kDriveMaxOpenLoopVoltageRampRate = 2.0;
    public static int kDriveCurrentLimitAmps = 50;
    public static int kDriveCurrentLimitTriggerDurationMs = 100;

    public static CheesyDriveGains kCheesyDriveGains = new CheesyDriveGains();

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
	public static double kNormalPercentThrottleReduction = 1.0;
	
	// These are applied AFTER the normal throttle reduction
    public static double kSnailModePercentThrottleReduction = 0.5;
    public static double kSnailModePercentRotateReduction = 0.4;
	
	// Applied after any scaling
    public static double kDriverInputTurnMaxMagnitude = 0.5;

    public static double kTurnInPlaceThrottleBump = 0.05;
    
	public static double kInputDeadbandF310Joystick = 0.05;
    public static double kInputDeadbandF310Trigger = 0.5;
    public static int kJoystickPortDriver = 0;
    public static int kJoystickPortOperator = 1;
    public static int kJoystickPortTester = 2;

    public static int kLimelightDefaultPipeline = ETrackingType.TARGET.getPipeline();
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
    public static int kDriveMotionMagicCruiseVelocity = 0;
    public static int kDriveMotionMagicMaxAccel = 0;

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

    public static double kElevatorClosedLoopMinPower = -1.0;
    public static double kElevatorClosedLoopMaxPower = 1.0;

    public static double kElevatorAllowableError = 1.0;
    public static int kElevatorNEOAddress = 15;

    public static int kElevatorSmartMotionSlot = 0;
    public static double kElevatorOpenLoopMinPower = -1.0;
    public static double kElevatorOpenLoopMaxPower = 1.0;

    public static double kElevatorOpenLoopRampRate = 0.1;
    public static int kElevatorSmartCurrentLimit = 60;
    public static int kElevatorSecondaryCurrentLimit = 80;

    // If the elevator's PDP slot draws more than this much current we flash the LEDs
    public static int kElevatorWarnCurrentLimitThreshold = 30;

    /*
    Smart Motion Constants

    Units are RPM
     */
    public static double kElevatorMotionP = 2.5e-4;
    public static double kElevatorMotionI = 0.0;
    public static double kElevatorMotionD = 0.0;
    public static double kElevatorMotionFF = 0.000391419;
    public static double kElevatorFrictionVoltage = 0.02 * 12.0;

    public static double kMaxElevatorVelocity = 4000;
    public static double kMinElevatorVelocity = 0;
    public static double kMaxElevatorAcceleration = 4000;
    public static double kElevatorClosedLoopAllowableError = 0; //The allowed deficit in rotations


    // =============================================================================
    // Closed-Loop Velocity Constants
    // =============================================================================
    public static int kDriveVelocityTolerance = 0;
    public static int kDriveVelocityLoopSlot = 0;
    public static double kDriveVelocity_kP = 1.0;
    public static double kDriveVelocity_kI = 0.0;
    public static double kDriveVelocity_kD = 0.0;
//    public static double kDriveVelocity_kF = (1023.0 / 1155.0);
    public static double kDriveVelocity_kF = 0.0; // We don't care about this feedforward because we inject our own with ArbitraryFeedforward

    // =============================================================================
    // Turn-To PID constants
    // =============================================================================
    public static PIDGains kPIDGains = new PIDGains( 0.0, 0.0, 0.0, 0.085 );
    public static double kTurnSensitivity = 0.85;
   
    // =============================================================================
    // Robot constants (configure later)
    // TO-DO: Configure torque constant
    // =============================================================================
    public static double kTFourBar = 0;
    public static double kFourBarPusherDelay = 0.5;

    // Pnuematic Intake
    public static double kPneumaticIntakeIntakePower = 0.7;

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

    //public static int kArmPositionEncoderTicksPerRotation = 3552;
    public static int kArmPositionEncoderTicksPerRotation = 4096;
    public static double kArmMinAngle = 0.0;
    public static double kArmMaxAngle = 135.0;
    public static double kArmMaxCurrentVoltRatio = 20; //tune - overcurrent ratio for arm motor
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
    public static double kArmKg = 1.1;

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

    public static double kIntakeWristPidP = 0.0;
    public static double kIntakeWristPidI = 0.0;
    public static double kIntakeWristPidD = 0.0;
    public static double kIntakeWristPidF = 0.008903875;
    public static int kIntakeWristAcceleration = 1000;
    // ticks per 100 ms, or N * 10 = ticks / sec
    public static int kIntakeWristCruise = 200;


    public static double kIntakeRollerHatchPower = .25;
    public static double kIntakeRollerCargoPower = .25;
    public static double kIntakeRollerHoldPower = .25;
    public static double kIntakeWristStowedAngle = 0;
    public static double kIntakeWristHandoffAngle = 48;
    // temp set to 90 to validate angles
    public static double kIntakeWristGroundAngle = 90;
    public static double kCargoSpitDelay = 4;
    // public static double kIntakeWristGroundAngle = 105;
    //The minimum angle where it is safe to continue intake process (engage solenoid/roller)
    public static double kIntakeWristGroundMinBound = 95;


    public static int kArmAcceleration = 5;
    public static int kArmCruise = 30;
    
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
    // PID TargetLock constants
    // =============================================================================
    public static PIDGains kTargetAngleLockGains = new PIDGains(0.0005, 0.000, 0.0);
    public static PIDGains kTargetDistanceLockGains = new PIDGains( 0.1, 0.0, 0.0);

    public static double kTargetAngleLockMinPower = -1.0;
    public static double kTargetAngleLockMaxPower = 1.0;
    public static double kTargetAngleLockMinInput = -27;
    public static double kTargetAngleLockMaxInput = 27;
    public static double kTargetAngleLockFrictionFeedforward = 0.44 / 12;
    public static double kTargetAngleLockLostTargetThreshold = 10;

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
        Ground(0.0), //The ground
        CargoHeight(6.5d);//This may change, not sure what the correct value

        private final double height;

        VisionTarget( double height)  {
            this.height = height;
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
    }

    // =============================================================================
    // Hatch Flower constants
    // =============================================================================
    public static double kHatchFlowerSolenoidReleaseTimeSec = 0.250;
    public static double kHatchFlowerPushDurationSec = 0.250;

    // kHatchFlowerGrabToPushTransitionTimeSec is the time between releasing the 
    // grab solenoid and engaging the push solenoid.
    public static double kHatchFlowerGrabToPushTransitionTimeSec = 0.250;
    public static double kHatchFlowerExtendStatusTimerDuration = 0.5;
    public static double kHatchFlowerReleaseDistance = 6.0;
    public static double kHatchFlowerReleaseTime = 1.0;


    // =============================================================================
    // 2019 Module Addresses
    // =============================================================================
    public static int kPigeonId = 30;
    public static int kCanifierAddress = 40;

    public static  int kDriveLeftMasterTalonId = 1;
    public static int kDriveLeftMiddleTalonId = 3;
    public static  int kDriveLeftRearTalonId = 5;
    public static  int kDriveRightMasterTalonId = 2;
    public static int kDriveRightMiddleTalonId = 4;
    public static  int kDriveRightRearTalonId = 6;

    public static EPowerDistPanel[] kDrivePdpSlots = new EPowerDistPanel[]{
            /* Left */
            EPowerDistPanel.CURRENT1,
            EPowerDistPanel.CURRENT2,

            /* Right */
            EPowerDistPanel.CURRENT13,
            EPowerDistPanel.CURRENT14,

    };

    public static int kPowerDistPanelAddress = 21;
    public static int kCargoSpitLeftSPXAddress = 13;
    public static int kCargoSpitRightSPXAddress = 14;
    public static int kCargoSpitBeamBreakAddress = 1;
    public static double kCargoSpitRollerPower = 0.20; // 15% seems like adequate power (maybe more?)
    public static double kCargoSpitSPXCurrentRatioLimit = 8.5; // Voltage ~ 1.8

    // TO-DO: Elevator encoder address?
    // public static int kElevatorRedundantEncoderAddress = -1;

    public static int kFourBarNEO1Address = 9;
    public static int kFourBarNEO2Address = 10;
    public static int kFourBarPusherAddress = 0;

    // TO-DO: label solenoid as forward/reverse in spreadsheet
    public static int kFourBarDoubleSolenoidForwardAddress = 0;
    public static int kFourBarDoubleSolenoidReverseAddress = 1;
    public static int kFourBarTBDSensorAddress = -1;

    public static double kFourBarP = 8.0e-4;
    public static double kFourBarI = 0.0;
    public static double kFourBarD = 0.0;
    public static double kFourBarF = 0.0;

    public static double kFourBarWarnCurrentLimitThreshold = 40;

    public static EPowerDistPanel[] kFourBarPdpSlots = new EPowerDistPanel[] {
            EPowerDistPanel.CURRENT0,
            EPowerDistPanel.CURRENT15
    };

    public static double kMaxFourBarVelocity = 2000;
    public static double kMinFourBarVelocity = 0;
    public static double kMaxFourBarAcceleration = 2000;
    public static double kFourBarClosedLoopAllowableError = 0;

    public static int kHatchFlowerOpenCloseSolenoidAddress = 5;
    public static int kHatchFlowerExtensionSolenoidAddress = 6;

    //public static int kHatchIntakeSPXAddress = 11;
    public static int kCargoIntakeSPXLowerAddress = 12;
    // TO-DO DO spreadsheet empty

    public static int kIntakeWristSRXAddress = 16;
    public static int kIntakeSolenoidAddress = 1;

}
