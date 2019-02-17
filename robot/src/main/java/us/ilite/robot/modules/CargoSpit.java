package us.ilite.robot.modules;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import com.team254.lib.drivers.TalonSRXFactory;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Solenoid;
import us.ilite.common.Data;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.types.manipulator.ECargoSpit;
import us.ilite.common.types.manipulator.EElevator;


public class CargoSpit extends Module {

    private ILog mLog = Logger.createLog(CargoSpit.class);

    private TalonSRX mLeftMotor, mRightMotor;
    // private Solenoid mSolenoid;
    private DigitalInput mSensor;
    private Data mData;
    private boolean mIntaking;
    private boolean mStopped;
    private double mPower = SystemSettings.kCargoSpitRollerPower; //TODO find actual value
    private boolean shouldIntake = false;
    private boolean shouldOuttake = false;


    public CargoSpit(Data pData) {

        this.mData = pData;

        // TODO Change to VictorSPX (or keep as TalonSRX)
        mLeftMotor = TalonSRXFactory.createDefaultTalon( SystemSettings.kCargoSpitLeftSPXAddress );//new VictorSPX(SystemSettings.kCargoSpitLeftSPXAddress);
        mRightMotor = TalonSRXFactory.createDefaultTalon( SystemSettings.kCargoSpitRightSPXAddress );//new VictorSPX(SystemSettings.kCargoSpitRightSPXAddress);

        //TODO figure out these values and make them constants
        mRightMotor.configOpenloopRamp( mPower, 5 );
        mLeftMotor.configOpenloopRamp( mPower, 5 );

        mLeftMotor.follow( mRightMotor );
        mLeftMotor.setInverted( true ); //Set one motor inverted

        // mSolenoid.close();
        mSensor.close();

        mIntaking = false;
        mStopped = false;

        mSensor = new DigitalInput( 0 );

    }

    @Override
    public void modeInit(double pNow) {
        mLog.error("MODE INIT");

    }

    @Override
    public void periodicInput(double pNow) {
        // TODO Read the PDP for current limiting check and compare to SystemSettings cargo spit current limit
    }

    @Override
    public void update(double pNow) {

        if(shouldIntake) {
            setIntaking();
        }
        if(shouldOuttake) {
            setOuttaking();
        }


        mData.cargospit.set( ECargoSpit.HAS_CARGO, convertBoolean( hasCargo() ) );
        mData.cargospit.set( ECargoSpit.INTAKING, convertBoolean( shouldIntake ) );
        mData.cargospit.set( ECargoSpit.OUTTAKING, convertBoolean( shouldOuttake ) );
        mData.cargospit.set( ECargoSpit.STOPPED, convertBoolean( mStopped ) );

    }

    @Override
    public void shutdown(double pNow) {

    }

    @Override
    public boolean checkModule(double pNow) {
        return false;
    }

    private void setIntaking() {

        if ( !mStopped || !hasCargo() ) {
            if ( !mIntaking ) {
                mIntaking = true;
                // mSolenoid.set( ECradleState.OPEN.getValue() ); //Values may be swapped?
            }

            mRightMotor.set( ControlMode.PercentOutput, mPower ); //TODO find actual value

            if ( hasCargo() ) {
                // mSolenoid.set( ECradleState.CLOSED.getValue() ); //Values may be swapped?
                mRightMotor.set( ControlMode.PercentOutput, 0 ); //Stop motor
            }
        }
        mStopped = false;
    }

    private void setOuttaking() {
        if ( !mStopped ) {
            mRightMotor.set( ControlMode.PercentOutput, -mPower );
            // mSolenoid.set( ECradleState.OPEN.getValue() );
        }
        mStopped = false;
    }

    public void stop() {
        mStopped = true;
    }

    public boolean hasCargo() {
        return mSensor.get();
    }

    public enum ECradleState {

        OPEN(false),
        CLOSED(true);

        boolean mActivate;

        ECradleState(boolean pActivate) {
            this.mActivate = pActivate;
        }

        boolean getValue() {
            return mActivate;
        }
    }

    public void setIntake(boolean pOn) {
        shouldIntake = pOn;
    }

    public void setOuttake( boolean pShouldOuttake ) {
        shouldOuttake = pShouldOuttake;
    }

    private double convertBoolean(boolean pToConvert) {
        if ( pToConvert ) {
            return 1d;
        }
        return 0d;
    }

    public boolean ismIntaking() {
        return mIntaking;
    }
}
