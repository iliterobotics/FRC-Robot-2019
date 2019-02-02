package us.ilite.robot.modules;

import com.ctre.phoenix.motorcontrol.can.VictorSPX;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import us.ilite.common.config.SystemSettings;


public class CargoSpit extends Module {

    private ILog mLog = Logger.createLog(CargoSpit.class);

    private VictorSPX spx;

    // TODO Read the PDP for current limiting check


    public CargoSpit() {
        // TODO Construction

        spx = new VictorSPX(SystemSettings.kCargoSpitSPXAddress);

    }

    @Override
    public void modeInit(double pNow) {
        mLog.error("MODE INIT");

    }

    @Override
    public void periodicInput(double pNow) {
        // TODO Read the spx current and compare to SystemSettings.kCargoSpitSPXCurrentLimit
        
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
