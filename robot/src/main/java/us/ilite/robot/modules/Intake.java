package us.ilite.robot.modules;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import com.team254.lib.drivers.talon.TalonSRXFactory;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Solenoid;
import us.ilite.common.Data;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.types.drive.EDriveData;
import us.ilite.common.types.manipulator.EIntake;
import us.ilite.common.types.sensor.EPowerDistPanel;


public class Intake extends Module {

    // TODO General: add flag for intake process; check code

    private final double kSafeAngle = SystemSettings.kIntakeWristAngleSafeSolenoidThreshold;
    private final boolean kHandoffSafe;
    private final double kZero = 0.0;
    private final String kDefaultError = "This message should not be displayed";
    private final double kTrueBoolean = 1.0;
    private final double kFalseBoolean = 0.0;

    private ILog mLog = Logger.createLog(Intake.class);

    private VictorSPX mIntakeRoller;

    // Wrist control
    private BasicArm mWrist;

    // Pneumatic for hatch/cargo roller mode toggle
    private Solenoid mSolenoid;

    // Hatch beam break sensor
    // private DigitalInput mHatchBeam;

    // Monitor the wrist SRX current angle
    private Double mWristAngle;
    // Monitor the roller current and voltage
    private double mIntakeRollerCurrent;
    private double mIntakeRollerVoltage;

    // Current intake states
    private EIntakeState mIntakeState;
    private EWristPosition mWristPosition;
    private ESolenoidState mSolenoidState;

    // Data class for codex stuff
    private Data mData;

    /**
     * Enumeration for the wrist position based on it's angle
     */
    public enum EWristPosition {
        GROUND(0.0), HANDOFF(0.0), STOWED(0.0);

        public final double kWristAngleDegrees;

        EWristPosition(double pWristAngle) {
            kWristAngleDegrees = pWristAngle;
        }

    }
    /**
     * Pneumatic/Solenoid state for hatch or cargo roller mode
     */
    public enum ESolenoidState {
        HATCH(false), CARGO(true);

        public final boolean kExtended;

        private ESolenoidState(boolean pSolenoid) {
            this.kExtended = pSolenoid;
        }

    }

    /**
     * The general intake state
     */
    public enum EIntakeState {
        GROUND_HATCH, GROUND_CARGO, HANDOFF, STOWED;
    }

    public Intake(Data pData) {
        // Basic Construction
        mIntakeRoller = new VictorSPX(SystemSettings.kHatchIntakeSPXAddress);
        this.mData = pData;
    
        // Wrist control
        TalonSRX tempTalonSRX = TalonSRXFactory.createDefaultTalon(SystemSettings.kIntakeWristSRXAddress);
        mWrist = new BasicArm(tempTalonSRX);

        // Solenoid for changing between cargo and hatch mode
        mSolenoid = new Solenoid(SystemSettings.kIntakeSolenoidAddress);

        // Sensor checking if hatch is in intake
        // mHatchBeam = new DigitalInput(SystemSettings.kIntakeBeamBreakAddress);

        // If the handoff angle is larger than the safe angle constraint
        kHandoffSafe = EWristPosition.HANDOFF.kWristAngleDegrees > kSafeAngle;
    }


    @Override
    public void modeInit(double pNow) {
        mLog.error("MODE INIT");
    }

    @Override
    public void periodicInput(double pNow) {
        mData.intake.set(EIntake.ARM_ANGLE, mWristAngle);
        mData.intake.set(EIntake.ROLLER_CURRENT, mIntakeRollerCurrent);
        mData.intake.set(EIntake.ROLLER_VOLTAGE, mIntakeRollerVoltage);
        mData.intake.set(EIntake.SOLENOID_EXTENDED, mSolenoid.get() ? kTrueBoolean : kFalseBoolean);
    }

    @Override
    public void update(double pNow) {
        // EPowerDistPanel ID 12 (CURRENT12) corresponds to Intake Rollers
        mIntakeRollerCurrent = mData.pdp.get(EPowerDistPanel.CURRENT12);
        mIntakeRollerVoltage = mIntakeRoller.getMotorOutputVoltage();
        if (mIntakeRollerCurrent > SystemSettings.kIntakeRollerCurrentLimit) {
            stopRoller();
        }

        mWristAngle = mWrist.getCurrentArmAngle();
    }

    @Override
    public void shutdown(double pNow) {

    }

    @Override
    public boolean checkModule(double pNow) {
        return false;
    }

    
    /**
     * Sets the wrist position to a preset position.
     * @param pWristPosition
     */
    public void setWrist(EWristPosition pWristPosition) {
        switch(pWristPosition) {
            case GROUND:
                mWrist.setArmAngle(EWristPosition.GROUND.kWristAngleDegrees);
                mWristPosition = EWristPosition.GROUND;
            case HANDOFF:
                mWrist.setArmAngle(EWristPosition.HANDOFF.kWristAngleDegrees);
                mWristPosition = EWristPosition.HANDOFF;
            case STOWED:
                mWrist.setArmAngle(EWristPosition.STOWED.kWristAngleDegrees);
                mWristPosition = EWristPosition.STOWED;
            default:
                mLog.error(kDefaultError);
        }
    }

    /**
     * The big boi method for controlling the intake.
     * @param pIntakeState 
     */
    public void setIntakeState(EIntakeState pIntakeState) {
        switch(pIntakeState) {
            case GROUND_HATCH:
                setWrist(EWristPosition.GROUND);
                setSolenoid(ESolenoidState.HATCH);
                mIntakeState = pIntakeState;

            case GROUND_CARGO:
                setWrist(EWristPosition.GROUND);
                setSolenoid(ESolenoidState.CARGO);
                mIntakeState = pIntakeState;

            case HANDOFF:
                // If handoff angle is not in the safe angle constraint
                if (!kHandoffSafe) {
                    // if arm at ground and solenoid is extended, retract the solenoid
                    if (mWristPosition.equals(EWristPosition.GROUND) && mSolenoidState.kExtended) {
                        setSolenoid(ESolenoidState.HATCH);
                    }
                }

                setWrist(EWristPosition.HANDOFF);
                mIntakeState = pIntakeState;

            case STOWED:
                // If the arm isn't already in stowed position and solenoid is extended, retract the solenoid
                if (!mWristPosition.equals(EWristPosition.STOWED) && mSolenoidState.kExtended) {
                    setSolenoid(ESolenoidState.HATCH);
                }

                setWrist(EWristPosition.STOWED);
                mIntakeState = pIntakeState;

            default:
                mLog.error(kDefaultError);
        }
    }

    /**
     * Sets the solenoid state.
     * i.e. sets whether the intake is in "cargo mode" or "hatch mode".
     * @param pExtended
     */
    public void setSolenoid(ESolenoidState pSolenoidState) {
        // If solenoid wants to deploy and wrist is at unsafe angle, don't deploy pneumatic; fail-safe check
        if (pSolenoidState.kExtended && mWristAngle > kSafeAngle) return;

        mSolenoid.set(pSolenoidState.kExtended);
        mSolenoidState = pSolenoidState;
    }

    // TODO Power depends on gamepiece? On robot speed? On both?
    /**
     * Changes the solenoid state.
     */
    public void setRollerPower(double pPower) {
        // Gets average speed: (left velocity + right velocity) / 2
        // Double speed = ( mData.drive.get(EDriveData.LEFT_VEL_IPS) + mData.drive.get(EDriveData.RIGHT_VEL_IPS) ) / 2;

        mIntakeRoller.set(ControlMode.PercentOutput, pPower);
    }

    /**
     * Sets roller current to zero.
     */
    public void stopRoller() {
        double current = kZero;
        mIntakeRoller.set(ControlMode.PercentOutput, current);
    }
    /**
     * Stops the wrist.
     */
    public void stopWrist() {
        double current = kZero;
        mWrist.setDesiredOutput(current);
    }

    /**
     * Stop the roller and wrist.
     */
    public void stop() {
        stopRoller();
        stopWrist();
    }

    /**
     * Stop rollers and set wrist to "handoff" position for cargo.
     * This method is used if we want to add something unique to cargo "handoff".
     */
    public void setHandoffCargo() {
        stopRoller();
        setWrist(EWristPosition.HANDOFF);
    }

    /**
     * Stop rollers and set wrist to "handoff" position for hatch.
     */
    public void setHandoffHatch() {
        stopRoller();
        setWrist(EWristPosition.HANDOFF);
    }

    // /**
    //  *
    //  * @return The sensor value indicating whether we have acquired a hatch.
    //  */
    // public boolean hasHatch() {
    //     return mHatchBeam.get();
    // }

    /**
     * Intake state getter method.
     */
    public EIntakeState getIntakeState() {
        return mIntakeState;
    }

    /**
     * Checks if the wrist is at a given position.
     * @param pWristPosition
     * @return Whether the wrist had been at the specified setpoint for a certain amount of time within a certain deadband.
     */
    public boolean isAtPosition(EWristPosition pWristPosition) {
        return mWristPosition.equals(pWristPosition);
    }

}
