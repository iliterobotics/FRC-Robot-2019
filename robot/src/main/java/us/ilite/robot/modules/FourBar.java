package us.ilite.robot.modules;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import com.revrobotics.CANEncoder;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import us.ilite.common.Data;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.types.EFourBarData;
import us.ilite.common.types.input.ELogitech310;


public class FourBar extends Module {

    private ILog mLog = Logger.createLog(FourBar.class);
    private Data mData = new Data();

    private CANSparkMax mNeo1;
    private CANSparkMax mNeo2;

    private CANEncoder mNeo1Encoder;
    private CANEncoder mNeo2Encoder;

    private DoubleSolenoid mDoubleSolenoid;

    private double mOutput;

    public FourBar() {
        // TODO Construction
        mNeo1 = new CANSparkMax(9, CANSparkMaxLowLevel.MotorType.kBrushless);
        mNeo2 = new CANSparkMax(10, CANSparkMaxLowLevel.MotorType.kBrushless);
    
        // Connect the NEO's to the encoders
        mNeo1Encoder = mNeo1.getEncoder();
        mNeo2Encoder = mNeo2.getEncoder();
    
        mDoubleSolenoid = new DoubleSolenoid(SystemSettings.kFourBarDoubleSolenoidForwardAddress, SystemSettings.kFourBarDoubleSolenoidReverseAddress);
    }


    @Override
    public void modeInit(double pNow) {
        mLog.error("FourBar Initialized...");
        mOutput = 0;
    }

    @Override
    public void periodicInput(double pNow) {
        
    }

    @Override
    public void update(double pNow) {
        if ( mData.driverinput.isSet( ELogitech310.B_BTN ) ) {
            mOutput = mData.driverinput.get( ELogitech310.LEFT_Y_AXIS );
            mNeo1.set( -mOutput );
            mNeo2.set( mOutput );
        } else {
            mNeo1.set( 0 );
            mNeo2.set( 0 );
        }
        updateCodex();
    }

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
