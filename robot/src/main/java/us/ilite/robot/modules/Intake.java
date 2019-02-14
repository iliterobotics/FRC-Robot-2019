package us.ilite.robot.modules;

import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import com.team254.lib.drivers.talon.TalonSRXFactory;

import us.ilite.common.config.SystemSettings;


public class Intake extends Module {

    private ILog mLog = Logger.createLog(Intake.class);

    private VictorSPX mIntakeRoller;

    // Wrist control
    private TalonSRX mWrist;

    // Monitor the wrist SRX current

    /**
     * Wrist presets
     */
    public enum EWristPosition {
        GROUND(0.0), HANDOFF(0.0), STOWED(0.0);

        public final double kWristAngleDegrees;

        EWristPosition(double pWristAngle) {
            kWristAngleDegrees = pWristAngle;
        }

    }

    public Intake() {
        // Construction
        mIntakeRoller = new VictorSPX(SystemSettings.kHatchIntakeSPXAddress);

        // TODO Add a "Beam Break" Sensor
    
        // Wrist control
        mWrist = TalonSRXFactory.createDefaultTalon(SystemSettings.kIntakeWristSRXAddress);
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

    }

    /**
     * Sets whether the roller arm is extended. In other words, sets whether the intake is in
     * "cargo mode" or "hatch mode". This method may be called periodically, not just once.
     * @param pExtended
     */
    public void setRollerExtended(boolean pExtended) {

    }

    // TODO Speed depends on gamepiece? On robot speed? On both?
    public void setRollerPower(double pPower) {

    }

    /**
     * Tells the intake to begin the intaking sequence for cargo. Sequence:
     * 1) Lower intake
     * 2) Extend the roller to "cargo" position
     * 3) Flag that the intaking sequence has started for cargo
     * 4) Set roller speed and current limit based on game piece type and (potentially) robot speed
     * This method may be called periodically, not just once.
     */
    public void setIntakingCargo() {
        setWrist(EWristPosition.GROUND);
        setRollerExtended(true);
//        setRollerPower();
    }

    /**
     * Tells the intake to begin the intaking sequence for the hatch. Sequence:
     * 1) Lower intake
     * 2) Retract the roller to "hatch" position
     * 3) Flag that the intaking sequence has started for hatch
     * 4) Set roller speed and current limit based on game piece type and (potentially) robot speed
     * 6) Stop roller if we have exceeded current limit for hatch intaking in update()
     * This method may be called periodically, not just once.
     */
    public void setIntakingHatch() {
        setWrist(EWristPosition.GROUND);
        setRollerExtended(false);
//        setRollerPower();
    }

    /**
     * Stop the roller and wrist
     */
    public void stop() {

    }

    /**
     * Stop rollers and set wrist to "handoff" position for cargo.
     * This method may be called periodically, not just once.
     */
    public void setHandoffCargo() {
        setWrist(EWristPosition.HANDOFF);
//        setRollerPower();
    }

    /**
     * Stop rollers and set wrist to "handoff" position for hatch.
     * This method may be called periodically, not just once.
     */
    public void setHandoffHatch() {
        setWrist(EWristPosition.HANDOFF);
    }

    /**
     * Stop rollers and set intake to "stowed" position.
     */
    public void stow() {

    }

    /**
     *
     * @return The sensor value indicating whether we have acquired a hatch.
     */
    public boolean hasHatch() {
        return true;
    }

    /**
     *
     * @param pWristPosition
     * @return Whether the wrist had been at the specified setpoint for a certain amount of time within a certain deadband.
     */
    public boolean isAtPosition(EWristPosition pWristPosition) {
        return true;
    }

}
