package us.ilite.robot.modules;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import com.revrobotics.CANEncoder;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import us.ilite.common.config.SystemSettings;


public class FourBar extends Module {

    private ILog mLog = Logger.createLog(FourBar.class);

    private CANSparkMax neo1;
    private CANSparkMax neo2;

    private CANEncoder neo1Encoder;
    private CANEncoder neo2Encoder;

    private DoubleSolenoid doubleSolenoid;


    public FourBar() {
        // TODO Construction
        neo1 = new CANSparkMax(SystemSettings.kFourBarNEO1Address, CANSparkMaxLowLevel.MotorType.kBrushless);
        neo2 = new CANSparkMax(SystemSettings.kFourBarNEO2Address, CANSparkMaxLowLevel.MotorType.kBrushless);
    
        // Connect the NEO's to the encoders
        neo1Encoder = new CANEncoder(neo1);
        neo2Encoder = new CANEncoder(neo2);
    
        doubleSolenoid = new DoubleSolenoid(SystemSettings.kFourBarDoubleSolenoidForwardAddress, SystemSettings.kFourBarDoubleSolenoidReverseAddress);
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
}
