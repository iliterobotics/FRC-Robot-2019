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

    public void setWrist(EWristPosition pWristPosition) {

    }

    public void setRollerExtended(boolean pExtended) {

    }

    // TODO Speed depends on gamepiece? On robot speed? On both?
    public void setRollerPower(double pPower) {

    }

    public void setIntakingCargo() {
        setWrist(EWristPosition.GROUND);
        setRollerExtended(true);
//        setRollerPower();
    }

    public void setIntakingHatch() {
        setWrist(EWristPosition.GROUND);
        setRollerExtended(false);
//        setRollerPower();
    }

    public void stop() {

    }

    public void setHandoffCargo() {
        setWrist(EWristPosition.HANDOFF);
//        setRollerPower();
    }

    public void setHandoffHatch() {
        setWrist(EWristPosition.HANDOFF);
    }

    public void stow() {

    }

    public boolean hasHatch() {
        return true;
    }

    public boolean isAtPosition(EWristPosition pWristPosition) {
        return true;
    }

}
