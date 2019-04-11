package us.ilite.robot.modules;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.StatusFrame;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

import com.team254.lib.drivers.talon.TalonSRXFactory;
import edu.wpi.first.wpilibj.DigitalInput;
import us.ilite.common.Data;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.types.manipulator.ECargoSpit;
import us.ilite.common.types.sensor.EPowerDistPanel;


public class CargoSpit extends Module {

    private final double kZero = 0.0;
    private final double kLaunchPower = 0.70;

    private ILog mLog = Logger.createLog(CargoSpit.class);

    private VictorSPX mLeftMotor, mRightMotor;
    private DigitalInput mBeambreak;
    private Data mData;
    private boolean mEmergencyStopped;
    private double mPower = SystemSettings.kCargoSpitRollerPower; //TODO find actual value
    private double mLeftCurrent, mRightCurrent;
    private boolean mIntaking = false;
    private boolean mOuttaking = false;
    private boolean mHasCargo = false;


    public CargoSpit(Data pData) {

        this.mData = pData;
        // TODO Change to VictorSPX (or keep as TalonSRX)
        mLeftMotor = TalonSRXFactory.createDefaultVictor(SystemSettings.kCargoSpitLeftSPXAddress);
//        mLeftMotor = new VictorSPX(SystemSettings.kCargoSpitLeftSPXAddress);
        mRightMotor = TalonSRXFactory.createDefaultVictor(SystemSettings.kCargoSpitRightSPXAddress);
//        mRightMotor = new VictorSPX(SystemSettings.kCargoSpitRightSPXAddress);

        mLeftMotor.enableVoltageCompensation(true);
        mRightMotor.enableVoltageCompensation(true);
        mLeftMotor.configVoltageCompSaturation(12.0);
        mRightMotor.configVoltageCompSaturation(12.0);

        mLeftMotor.setStatusFramePeriod(StatusFrame.Status_4_AinTempVbat, 5);
        mRightMotor.setStatusFramePeriod(StatusFrame.Status_4_AinTempVbat, 5);

        mBeambreak = new DigitalInput(SystemSettings.kCargoSpitBeamBreakAddress);

        //TODO figure out these values and make them constants
        mRightMotor.configOpenloopRamp( mPower, 5 );
        mLeftMotor.configOpenloopRamp( mPower, 5 );

        // mRightMotor.follow( mLeftMotor );
        mLeftMotor.setInverted(false);
        mRightMotor.setInverted( true ); //Set one motor inverted

        mIntaking = false;
        mOuttaking = false;
        mEmergencyStopped = true;
    }

    @Override
    public void modeInit(double pNow) {
        mLog.error("MODE INIT");

    }

    @Override
    public void periodicInput(double pNow) {
        // TODO Read the PDP for current limiting check and compare to SystemSettings cargo spit current limit
        mData.cargospit.set( ECargoSpit.HAS_CARGO, mHasCargo ? 1.0 : 0.0);
        mData.cargospit.set( ECargoSpit.INTAKING, convertBoolean( mIntaking ) );
        mData.cargospit.set( ECargoSpit.OUTTAKING, convertBoolean( mOuttaking ) );
        mData.cargospit.set( ECargoSpit.STOPPED, convertBoolean( mEmergencyStopped ) );
        mData.cargospit.set( ECargoSpit.LEFT_CURRENT, mLeftCurrent );
        mData.cargospit.set( ECargoSpit.RIGHT_CURRENT, mRightCurrent );
        mData.cargospit.set( ECargoSpit.BEAM_BROKEN, convertBoolean( isBeamBroken() ));
    }

    @Override
    public void update(double pNow) {
        mLeftCurrent = mData.pdp.get(EPowerDistPanel.CURRENT10);
        mRightCurrent = mData.pdp.get(EPowerDistPanel.CURRENT5);
        mHasCargo = shouldStop();
        if ( mHasCargo ) {
            stop();
        }
    }

    public void setIntaking() {
        if ( !mEmergencyStopped || !shouldStop() ) {
            mIntaking = true;
            mOuttaking = false;
            mLeftMotor.set( ControlMode.PercentOutput, mPower );
            mRightMotor.set( ControlMode.PercentOutput, mPower );
            if ( shouldStop() ) {
                mLeftMotor.set( ControlMode.PercentOutput, kZero );
                mRightMotor.set( ControlMode.PercentOutput, kZero );
            }
        }
        mEmergencyStopped = false;
    }

    public void setOuttaking() {
        if ( !mEmergencyStopped ) {
            mIntaking = false;
            mOuttaking = true;
            mLeftMotor.set( ControlMode.PercentOutput, -kLaunchPower );
            mRightMotor.set( ControlMode. PercentOutput, -kLaunchPower );
        }
        mEmergencyStopped = false;
    }

    private boolean shouldStop() {
        if ( mOuttaking ) {
            return false;
        }

        return isCurrentLimiting() || isBeamBroken();
    }

    public boolean isCurrentLimiting() {
        double currentLimit = SystemSettings.kCargoSpitSPXCurrentRatioLimit;
        // Ratio being current over voltage
        // Voltage will be assumed to be 12 to avoid polling CAN so much
        // This *should* be okay since we enabled voltage comp
        double leftRatio = mLeftCurrent / 12.0;
        double rightRatio = mRightCurrent / 12.0;
        double averageRatio = ( leftRatio + rightRatio ) / 2.0;

        return (averageRatio >= currentLimit);
    }

    public boolean hasCargo() {
        return mHasCargo;
    }

    public boolean isIntaking() {
        return mIntaking;
    }
    public boolean isOuttaking() { return mOuttaking; }
    
    public void stop() {
        mLeftMotor.set( ControlMode.PercentOutput, kZero );
        mRightMotor.set( ControlMode.PercentOutput, kZero );
        mIntaking = false;
        mOuttaking = false;
    }

    public void emergencyStop() {
        mEmergencyStopped = true;
    }

    public boolean isBeamBroken() {
        // Beam break returns true when not broken
        return mBeambreak.get();
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
