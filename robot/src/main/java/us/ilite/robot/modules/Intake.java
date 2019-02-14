package us.ilite.robot.modules;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import com.team254.lib.drivers.talon.TalonSRXFactory;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Solenoid;
import us.ilite.common.config.SystemSettings;


public class Intake extends Module {

    // TODO General: add pneumatic/wrist constraint; add flag for intake process; check code

    private ILog mLog = Logger.createLog(Intake.class);

    private VictorSPX mIntakeRoller;

    // Wrist control
    private BasicArm mWrist;

    // Pneumatic for hatch/cargo mode toggle
    private Solenoid mSolenoid;

    // Hatch beam break sensor
    private DigitalInput mHatchBeam;

    // Monitor the wrist SRX current angle
    private Double mWristAngle;

    // Current wrist state
    private EWristPosition mWristPosition;

    public enum EWristPosition {
        GROUND(0.0), HANDOFF(0.0), STOWED(0.0);

        public final double kWristAngleDegrees;

        EWristPosition(double pWristAngle) {
            kWristAngleDegrees = pWristAngle;
        }

    }
    public enum ESolenoidState {
        HATCH(false), CARGO(true);

        public final boolean kActive;

        private ESolenoidState(boolean pSolenoid) {
            this.kActive = pSolenoid;
        }

    }

    public Intake() {
        // Construction
        mIntakeRoller = new VictorSPX(SystemSettings.kHatchIntakeSPXAddress);
    
        // Wrist control
        mWrist = new BasicArm(TalonSRXFactory.createDefaultTalon(SystemSettings.kIntakeWristSRXAddress));

        // Solenoid for changing between cargo and hatch mode
        mSolenoid = new Solenoid(SystemSettings.kIntakeSolenoidAddress);

        // Sensor checking if hatch is in intake
        mHatchBeam = new DigitalInput(SystemSettings.kIntakeBeamBreakAddress);
    }

    @Override
    public void modeInit(double pNow) {
        mLog.error("MODE INIT");
    }

    @Override
    public void periodicInput(double pNow) {
        
    }

    @Override
    public void update(double pNow) {
        // TODO Check this
        if (mIntakeRoller.getMotorOutputPercent() > SystemSettings.kIntakeRollerCurrentLimit) {
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
                mLog.error("This message should not be displayed");
        }
    }

    /**
     * Sets whether the roller arm is extended. In other words, sets whether the intake is in
     * "cargo mode" or "hatch mode".
     * @param pExtended
     */
    public void setRollerExtended(ESolenoidState pSolenoidState) {
        mSolenoid.set(pSolenoidState.kActive);
    }

    // TODO Speed depends on gamepiece? On robot speed? On both?
    public void setRollerPower(double pPower) {
        mIntakeRoller.set(ControlMode.PercentOutput, pPower);
    }

    /**
     * Sets roller current to zero
     */
    public void stopRoller() {
        double curr = 0.0;
        mIntakeRoller.set(ControlMode.PercentOutput, curr);
    }
    /**
     * Stops wrist
     */
    public void stopWrist() {
        double curr = 0.0;
        mWrist.setDesiredOutput(curr);
    }

    /**
     * Tells the intake to begin the intaking sequence for cargo. Sequence:
     * 1) Lower intake
     * 2) Extend the roller to "cargo" position
     * 3) Flag that the intaking sequence has started for cargo
     * 4) Set roller speed and current limit based on game piece type and (potentially) robot speed
     */
    public void setIntakingCargo() {
        setWrist(EWristPosition.GROUND);
        setRollerExtended(ESolenoidState.CARGO);
        // TODO step 3 flag
       setRollerPower(SystemSettings.kIntakeRollerCargoPower);
    }

    /**
     * Tells the intake to begin the intaking sequence for the hatch. Sequence:
     * 1) Lower intake
     * 2) Retract the roller to "hatch" position
     * 3) Flag that the intaking sequence has started for hatch
     * 4) Set roller speed and current limit based on game piece type and (potentially) robot speed
     * 6) Stop roller if we have exceeded current limit for hatch intaking in update()
     */
    public void setIntakingHatch() {
        setWrist(EWristPosition.GROUND);
        setRollerExtended(ESolenoidState.HATCH);
        // TODO step 3 flag
        setRollerPower(SystemSettings.kIntakeRollerHatchPower);
    }

    /**
     * Stop the roller and wrist
     */
    public void stop() {
        stopRoller();
        stopWrist();
    }

    /**
     * Stop rollers and set wrist to "handoff" position for cargo.
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

    /**
     *
     * @return The sensor value indicating whether we have acquired a hatch.
     */
    public boolean hasHatch() {
        return mHatchBeam.get();
    }

    /**
     *
     * @param pWristPosition
     * @return Whether the wrist had been at the specified setpoint for a certain amount of time within a certain deadband.
     */
    public boolean isAtPosition(EWristPosition pWristPosition) {
        return mWristPosition.equals(pWristPosition);
    }

}
