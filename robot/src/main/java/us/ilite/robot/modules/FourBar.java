package us.ilite.robot.modules;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import com.revrobotics.CANEncoder;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import us.ilite.common.Data;
import us.ilite.common.config.SystemSettings;


public class FourBar extends Module {

    private ILog mLog = Logger.createLog(FourBar.class);
    private Data mData = new Data();

    private CANSparkMax mNeo1;
    private CANSparkMax mNeo2;

    private CANEncoder mNeo1Encoder;
    private CANEncoder mNeo2Encoder;

    private DoubleSolenoid mDoubleSolenoid;


    public FourBar() {
        // TODO Construction
        mNeo1 = new CANSparkMax(SystemSettings.kFourBarNEO1Address, CANSparkMaxLowLevel.MotorType.kBrushless);
        mNeo2 = new CANSparkMax(SystemSettings.kFourBarNEO2Address, CANSparkMaxLowLevel.MotorType.kBrushless);
    
        // Connect the NEO's to the encoders
        mNeo1Encoder = new CANEncoder( mNeo1 );
        mNeo2Encoder = new CANEncoder( mNeo2 );
    
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
            updateCodex();
    public void updateCodex() {
        // TO-DO: log angle of fourbar
        mData.fourbar.set( EFourBarData.A_OUTPUT, -mOutput );
        mData.fourbar.set( EFourBarData.A_VOLTAGE, mNeo1.getBusVoltage() );
        mData.fourbar.set( EFourBarData.A_CURRENT, mNeo1.getOutputCurrent() );

        mData.fourbar.set( EFourBarData.B_OUTPUT, mOutput );
        mData.fourbar.set( EFourBarData.B_VOLTAGE, mNeo2.getBusVoltage() );
        mData.fourbar.set( EFourBarData.B_CURRENT, mNeo2.getOutputCurrent() );
    }

    @Override
    public void shutdown(double pNow) {

    }
}
