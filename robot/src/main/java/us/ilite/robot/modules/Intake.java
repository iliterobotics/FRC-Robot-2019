package us.ilite.robot.modules;

import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import com.team254.lib.drivers.talon.TalonSRXFactory;

import edu.wpi.first.wpilibj.Encoder;
import us.ilite.common.config.SystemSettings;
import us.ilite.robot.hardware.BeamBreak;


public class Intake extends Module {

    private ILog mLog = Logger.createLog(Intake.class);

    private VictorSPX spx1;
    private VictorSPX spx2;

    private BeamBreak beamBreak;

    // Wrist control
    private TalonSRX wristSRX;
    private Encoder encoder;

    // Monitor the wrist SRX current
    
    public Intake() {
        // TODO Construction
        spx1 = new VictorSPX(SystemSettings.kIntakeSPX1Address);
        spx2 = new VictorSPX(SystemSettings.kIntakeSPX2Address);
    
        beamBreak = new BeamBreak(SystemSettings.kIntakeBeamBreakAddress);
    
        // Wrist control
        wristSRX = TalonSRXFactory.createDefaultTalon(SystemSettings.kIntakeWristSRXAddress);
        encoder = new Encoder(SystemSettings.kIntakeWristEncoderA_Address, SystemSettings.kIntakeWristEncoderB_Address);
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

}
