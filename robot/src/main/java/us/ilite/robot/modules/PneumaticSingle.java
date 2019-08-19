package us.ilite.robot.modules;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;
import com.team254.lib.drivers.talon.TalonSRXFactory;
import edu.wpi.first.wpilibj.Solenoid;
import us.ilite.common.Data;
import us.ilite.common.config.SystemSettings;
import us.ilite.robot.hardware.SolenoidWrapper;

public class PneumaticSingle extends Module {
    private static PneumaticSingle pneumaticInstance = new PneumaticSingle();

    PneumaticSingle.EPneumaticIntakePosition mDesiredPosition;
    PneumaticSingle.EPneumaticIntakePosition mCurrentPosition;
    VictorSPX mRollerVictor;

    private Solenoid mIntake;
    private SolenoidWrapper mIntakeSolenoid;
    private Data mData;
    private double mPower = 0;

    private PneumaticSingle () {
        this.mDesiredPosition = PneumaticSingle.EPneumaticIntakePosition.STOWED;
        this.mCurrentPosition = PneumaticSingle.EPneumaticIntakePosition.STOWED;
        this.mIntake = new Solenoid( SystemSettings.kCANAddressPCM, 1 );
        this.mIntakeSolenoid = new SolenoidWrapper(mIntake);
        mRollerVictor = TalonSRXFactory.createDefaultVictor( SystemSettings.kCargoIntakeSPXLowerAddress );
    }

    public void setData( Data pData ) {
        this.mData = pData;
    }

    public enum EPneumaticIntakePosition {
        STOWED,
        INTAKING,
        OUTTAKING;
    }

    @Override
    public void modeInit( double pNow ) {

    }

    @Override
    public void periodicInput( double pNow ) {

    }

    @Override
    public void update( double pNow ) {

        switch(mDesiredPosition) {
            case STOWED:
                mIntakeSolenoid.set( false );
                deactivateRoller();
                break;
            case INTAKING:
                mIntakeSolenoid.set( true );
                activateRoller();
                break;
            case OUTTAKING:
                mIntakeSolenoid.set( true );
                setRollerOuttaking();
                break;
            default:
                break;
        }
        mRollerVictor.set( ControlMode.PercentOutput, mPower );
//        mData.kLoggingTable.putString( "Pneumatic Desired Position", mDesiredPosition.toString() );
//        mData.kLoggingTable.putDouble( "Pneumatic Desired Roller Power", mPower );
    }

    @Override
    public void shutdown( double pNow ) {

    }

    private void activateRoller() {
        mPower = SystemSettings.kPneumaticIntakeIntakePower;
    }

    private void setRollerOuttaking() {
        mPower = -0.50;
    }

    private void deactivateRoller() {
        mPower = 0;
    }

    public void setDesiredPosition( PneumaticSingle.EPneumaticIntakePosition pDesiredPosition) {
        mDesiredPosition = pDesiredPosition;
    }

    public static PneumaticSingle getInstance() {
        return pneumaticInstance;
    }


}
