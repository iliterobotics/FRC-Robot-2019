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

    private final double kSafeAngle = SystemSettings.kIntakeWristAngleSafeSolenoidThreshold;
    // private final boolean kHandoffSafe;
    private final double kZero = 0.0;
    private final String kDefaultError = "This message should not be displayed";
    private final double kTrueBoolean = 1.0;
    private final double kFalseBoolean = 0.0;

    // Logging fields
    private ILog mLog = Logger.createLog(Intake.class);
    private Data mData;

    // Roller fields
    private VictorSPX mIntakeRoller; //14
    private boolean mHasHatch;
    private double mIntakeRollerCurrent;
    private double mIntakeRollerVoltage;

    // Wrist/Arm fields
    private BasicArm mWrist;
    private Double mWristAngle;

    // Pneumatic/Solenoid for hatch/cargo roller mode toggle
    private Solenoid mSolenoid;

    // Hatch beam break sensor
    // private DigitalInput mHatchBeam;

    // Present intake states
    private EIntakeState mIntakeState;
    private EWristPosition mWristPosition;
    private ESolenoidState mSolenoidState;

    /**
     * Wrist position based on angle
     */
    public enum EWristPosition {
        // TODO update wrist position angles
        GROUND(5.0), HANDOFF(15.0), STOWED(25.0);

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

        private ESolenoidState(boolean pExtended) {
            this.kExtended = pExtended;
        }

    }

    /**
     * The general intake state
     */
    public enum EIntakeState {
        GROUND_HATCH, GROUND_CARGO, HANDOFF, STOWED, STOPPED;
    }

    public Intake(Data pData) {
        // Basic Construction
        // mIntakeRoller = new VictorSPX(SystemSettings.kHatchIntakeSPXAddress);
        mIntakeRoller = new VictorSPX(14);
        this.mData = pData;
    
        // Wrist control
        // TalonSRX tempTalonSRX = TalonSRXFactory.createDefaultTalon(SystemSettings.kIntakeWristSRXAddress); //11
        TalonSRX tempTalonSRX = TalonSRXFactory.createDefaultTalon(11);
        mWrist = new BasicArm(tempTalonSRX);

        // Solenoid for changing between cargo and hatch mode
        // mSolenoid = new Solenoid(SystemSettings.kIntakeSolenoidAddress);
        mSolenoid = new Solenoid(5);

        mSolenoidState = ESolenoidState.HATCH;
        mIntakeState = EIntakeState.STOWED;
        mWristPosition = EWristPosition.STOWED;

        // Sensor checking if hatch is in intake
        // mHatchBeam = new DigitalInput(SystemSettings.kIntakeBeamBreakAddress);

        // If the handoff angle is larger than the safe angle constraint
        // kHandoffSafe = EWristPosition.HANDOFF.kWristAngleDegrees > kSafeAngle;
    }


    @Override
    public void modeInit(double pNow) {
        mLog.error("MODE INIT");
    }

    @Override
    public void periodicInput(double pNow) {
        // mData.intake.set(EIntake.ARM_ANGLE, mWristAngle);
        // mData.intake.set(EIntake.ROLLER_CURRENT, mIntakeRollerCurrent);
        // mData.intake.set(EIntake.ROLLER_VOLTAGE, mIntakeRollerVoltage);
        // mData.intake.set(EIntake.SOLENOID_EXTENDED, mSolenoid.get() ? kTrueBoolean : kFalseBoolean);
    }

    @Override
    public void update(double pNow) {
        // EPowerDistPanel ID 12 (CURRENT12) corresponds to Intake Rollers
        // mIntakeRollerCurrent = mData.pdp.get(EPowerDistPanel.CURRENT12);
        // mIntakeRollerVoltage = mIntakeRoller.getMotorOutputVoltage();
        // if (mIntakeRollerCurrent > SystemSettings.kIntakeRollerCurrentLimit) {
        //     stopRoller();
        //     mHasHatch = true;
        // }

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
     * @param pWristPosition desired wrist position
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
     * @param pIntakeState desired intake state
     */
    public void setIntakeState(EIntakeState pIntakeState) {
        // stopIntake();
        switch(pIntakeState) {
            case GROUND_HATCH:
                mHasHatch = false;
                setWrist(EWristPosition.GROUND);
                setSolenoidState(ESolenoidState.HATCH);
                setRoller();
                mIntakeState = pIntakeState;

            case GROUND_CARGO:
                mHasHatch = false;
                setWrist(EWristPosition.GROUND);
                setSolenoidState(ESolenoidState.CARGO);
                setRoller();
                mIntakeState = pIntakeState;

            case HANDOFF:
                // TODO may or may not need: adds resistance to roller to prevent hatch slippage
                if (mHasHatch) {
                    setRollerResistance();
                }
                // If handoff angle is not in the safe angle constraint
                // if (!kHandoffSafe) {
                    // if arm at ground and solenoid is extended, retract the solenoid
                    if (mWristPosition.equals(EWristPosition.GROUND) && mSolenoidState.kExtended) {
                        setSolenoidState(ESolenoidState.HATCH);
                    }
                // }

                setWrist(EWristPosition.HANDOFF);
                mIntakeState = pIntakeState;

            case STOWED:
                mHasHatch = false;
                stopRoller();
                // If the arm isn't already in stowed position and solenoid is extended, retract the solenoid
                if (!mWristPosition.equals(EWristPosition.STOWED) && mSolenoidState.kExtended) {
                    setSolenoidState(ESolenoidState.HATCH);
                }

                setWrist(EWristPosition.STOWED);
                mIntakeState = pIntakeState;

            case STOPPED:
                stopIntake();
                
            default:
                mLog.error(kDefaultError);
        }
    }

    public void commandHandoff() {
        // setIntakeState(EIntakeState.HANDOFF);
        setRollerPower(0.5);
    }
    public void commandGround() {
        setIntakeState(EIntakeState.GROUND_HATCH);
    }
    public void commandSolenoid() {
        if (mSolenoidState.kExtended) {
            setSolenoidState(ESolenoidState.HATCH);
        }
        else {
            setSolenoidState(ESolenoidState.CARGO);
        }
    }
    public void commandStop() {
        setIntakeState(EIntakeState.STOPPED);
    }

    /**
     * Main method for controlling the rollers.
     * Roller power is based on current solenoid state (HATCH/CARGO).
     */
    public void setRoller() {
        switch(mSolenoidState) {
            case HATCH:
                setRollerPower(SystemSettings.kIntakeRollerHatchPower);
            case CARGO:
                setRollerPower(SystemSettings.kIntakeRollerCargoPower);
            default:
                mLog.error(kDefaultError);
        }
    }
    /**
     * Gives rollers a small amount of resistance so hatch doesn't fall out
     */
    public void setRollerResistance() {
        setRollerPower(SystemSettings.kIntakeRollerResistancePower);
    }
    // TODO Power depends on gamepiece? On robot speed? On both?
    /**
     * Starts the intake roller.
     * @param pPower percent of power to use
     */
    public void setRollerPower(double pPower) {
        // Gets average speed: (left velocity + right velocity) / 2
        // Double speed = ( mData.drive.get(EDriveData.LEFT_VEL_IPS) + mData.drive.get(EDriveData.RIGHT_VEL_IPS) ) / 2;

        mIntakeRoller.set(ControlMode.PercentOutput, pPower);
    }

    /**
     * Sets the solenoid state.
     * i.e. sets whether the intake is in "cargo mode" or "hatch mode".
     * @param pSolenoidState desired solenoid state
     */
    public void setSolenoidState(ESolenoidState pSolenoidState) {
        // If solenoid wants to deploy and wrist is at unsafe angle, don't deploy pneumatic; fail-safe check
        // if (pSolenoidState.kExtended && mWristAngle > kSafeAngle) return;

        mSolenoid.set(pSolenoidState.kExtended);
        mSolenoidState = pSolenoidState;
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
    public void stopIntake() {
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
     * @return returns intake state
     */
    public EIntakeState getIntakeState() {
        return mIntakeState;
    }

    /**
     * @return is hatch in intake
     */
    public boolean getHasHatch() {
        return mHasHatch;
    }

    /**
     * Asks if the a given wrist position matches the current wrist position.
     * @param pWristPosition position in question
     * @return Whether the wrist had been at the specified setpoint for a certain amount of time within a certain deadband.
     */
    public boolean isAtPosition(EWristPosition pWristPosition) {
        return mWristPosition.equals(pWristPosition);
    }

}
