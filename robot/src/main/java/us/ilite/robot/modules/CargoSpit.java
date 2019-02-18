package us.ilite.robot.modules;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import com.team254.lib.drivers.TalonSRXFactory;
import us.ilite.common.Data;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.types.manipulator.ECargoSpit;
import us.ilite.common.types.manipulator.EElevator;


public class CargoSpit extends Module {

    private ILog mLog = Logger.createLog(CargoSpit.class);

    private TalonSRX mLeftMotor, mRightMotor;
    private Data mData;
    private boolean mIntaking;
    private boolean mStopped;
    private double mPower = SystemSettings.kCargoSpitRollerPower; //TODO find actual value
    private boolean shouldIntake = false;
    private boolean shouldOuttake = false;


    public CargoSpit(Data pData) {

        this.mData = pData;
        // TODO Change to VictorSPX (or keep as TalonSRX)
        mLeftMotor = TalonSRXFactory.createDefaultTalon( 3/*SystemSettings.kCargoSpitLeftSPXAddress*/ );//new VictorSPX(SystemSettings.kCargoSpitLeftSPXAddress);
        mRightMotor = TalonSRXFactory.createDefaultTalon( 4/*SystemSettings.kCargoSpitRightSPXAddress*/ );//new VictorSPX(SystemSettings.kCargoSpitRightSPXAddress);
        //TODO figure out these values and make them constants
        mRightMotor.configOpenloopRamp( mPower, 5 );
        mLeftMotor.configOpenloopRamp( mPower, 5 );

        mRightMotor.follow( mLeftMotor );
        mRightMotor.setInverted( true ); //Set one motor inverted

        mIntaking = false;
        mStopped = true;
    }

    @Override
    public void modeInit(double pNow) {
        mLog.error("MODE INIT");

    }

    @Override
    public void periodicInput(double pNow) {
        // TODO Read the PDP for current limiting check and compare to SystemSettings cargo spit current limit
        mData.cargospit.set( ECargoSpit.HAS_CARGO, convertBoolean( hasCargo() ) );
        mData.cargospit.set( ECargoSpit.INTAKING, convertBoolean( shouldIntake ) );
        mData.cargospit.set( ECargoSpit.OUTTAKING, convertBoolean( shouldOuttake ) );
        mData.cargospit.set( ECargoSpit.STOPPED, convertBoolean( mStopped ) );
    }

    @Override
    public void update(double pNow) {
        if(shouldIntake) {
            setIntaking();
        }
        if(shouldOuttake) {
            setOuttaking();
        }
    }

    public void setIntaking() {
        if ( !mStopped || !hasCargo() ) {
            if ( !mIntaking ) {
                mIntaking = true;
            }
            mLeftMotor.set( ControlMode.PercentOutput, mPower );
            if ( hasCargo() ) {
                mLeftMotor.set( ControlMode.PercentOutput, mPower );
            }
        }
        mStopped = false;
    }

    private void setOuttaking() {
        if ( !mStopped ) {
            mLeftMotor.set( ControlMode.PercentOutput, -mPower );
        }
        mStopped = false;
    }

    public void setIntake(boolean pOn) {
        shouldIntake = pOn;
    }

    public void setOuttake( boolean pShouldOuttake ) {
        shouldOuttake = pShouldOuttake;
    }

    public boolean hasCargo() {
        return mLeftMotor.getOutputCurrent() >= SystemSettings.kCargoSpitSPXCurrentLimit;
    }

    public boolean isIntaking() {
        return mIntaking;
    }
    
    public void stop() {
        mStopped = true;
    }

    private double convertBoolean(boolean pToConvert) {
        if ( pToConvert ) {
            return 1d;
        }
        return 0d;
    }

    @Override
    public boolean checkModule(double pNow) {
        return false;
    }

    @Override
    public void shutdown(double pNow) {
    }
}
