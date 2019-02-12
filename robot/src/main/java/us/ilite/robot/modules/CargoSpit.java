package us.ilite.robot.modules;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Solenoid;
import us.ilite.common.config.SystemSettings;


public class CargoSpit extends Module {

    private ILog mLog = Logger.createLog(CargoSpit.class);

    private VictorSPX mLeftMotor, mRightMotor;
    private Solenoid mSolenoid;
    private DigitalInput mSensor = new DigitalInput( 0 ); //Todo figure out channel
    private boolean mIntaking;
    private boolean mStopped;

    // TODO Read the PDP for current limiting check


    public CargoSpit() {


        mLeftMotor = new VictorSPX(SystemSettings.kCargoSpitLeftSPXAddress);
        mRightMotor = new VictorSPX(SystemSettings.kCargoSpitRightSPXAddress);

        //TODO figure out these values and make them constants
        mRightMotor.configOpenloopRamp( 0.5, 5 );
        mLeftMotor.configOpenloopRamp( 0.5, 5 );

        mLeftMotor.follow( mRightMotor );
        mLeftMotor.setInverted( true ); //Set one motor inverted

        mSolenoid.close();
        mSensor.close();

        mIntaking = false;
        mStopped = false;

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

    public void setIntaking() {

        if ( !mStopped || !hasCargo() ) {
            if ( !mIntaking ) {
                mIntaking = true;
                mSolenoid.set( true ); //Values may be swapped?
            }

            mRightMotor.set( ControlMode.PercentOutput, 0.5 ); //TODO find actual value

            if ( hasCargo() ) {
                mSolenoid.set( false ); //Values may be swapped?
                mRightMotor.set( ControlMode.PercentOutput, 0 );
            }
        }
    }

    public void setOuttaking() {
        if ( !mStopped ) {
            
        }
    }

    public void stop() {
        mStopped = true;
    }

    public boolean hasCargo() {
        return mSensor.get();
    }

}
