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


public class FourBar extends Module {

    private ILog mLog = Logger.createLog(FourBar.class);
    private Data mData;

    private CANSparkMax mNeo1;
    private CANSparkMax mNeo2;
    private CANEncoder mNeo1Encoder;
    private CANEncoder mNeo2Encoder;
    private DoubleSolenoid mDoubleSolenoid;

    public double mAngularPosition;
    private double mPreviousNeo1Rotations;
    private double mPreviousNeo2Rotations;

    // varying output according to angle of fourbar to counteract gravity
    private double mGravityComp;
    private double mOutput;

    public FourBar( Data pData ) {
        // Later: SystemSettings address
        mNeo1 = new CANSparkMax(9, CANSparkMaxLowLevel.MotorType.kBrushless);
        mNeo2 = new CANSparkMax(10, CANSparkMaxLowLevel.MotorType.kBrushless);
    
        // Connect the NEO's to the encoders
        mNeo1Encoder = mNeo1.getEncoder();
        mNeo2Encoder = mNeo2.getEncoder();
    
        // mDoubleSolenoid = new DoubleSolenoid(SystemSettings.kFourBarDoubleSolenoidForwardAddress, SystemSettings.kFourBarDoubleSolenoidReverseAddress);

        mAngularPosition = ( ( mNeo1Encoder.getPosition() / 300 ) + ( mNeo2Encoder.getPosition() / 300 ) ) / 2;
        mGravityComp = SystemSettings.kMass * 10 * Math.cos( mAngularPosition ) * SystemSettings.kFourBarCenterOfGravity * SystemSettings.kT;
        mData = pData;
    }


    @Override
    public void modeInit(double pNow) {
        mLog.error("FourBar Initialized...");
        mOutput = 0;
        mPreviousNeo1Rotations = mNeo1Encoder.getPosition();
        mPreviousNeo2Rotations = mNeo2Encoder.getPosition();

        mNeo1.setSmartCurrentLimit( 20 );
        mNeo2.setSmartCurrentLimit( 20 );
    }

    @Override
    public void periodicInput(double pNow) {
        updateCodex();
    }

    @Override
    public void update(double pNow) {
        mNeo1.set( -mOutput );
        mNeo2.set( mOutput );
        updateCodex();
    }

    @Override
    public void shutdown(double pNow) {
        mNeo1.disable();
        mNeo2.disable();
    }

    // later use states to determine output
    public void setDesiredOutput( double output, boolean isIdle ) {
        if ( isIdle ) {
            mOutput = 0;
        } else {
            mOutput = output + mGravityComp;
        }
        updateCodex();
    }

    public double gravityCompAtPosition() {
        return SystemSettings.kMass * 10 * Math.cos( mAngularPosition ) * SystemSettings.kFourBarCenterOfGravity * SystemSettings.kT;
    }

    public void updateAngularPosition() {
        mAngularPosition = ( ( mNeo1Encoder.getPosition() - mPreviousNeo1Rotations / 300 ) + ( mNeo2Encoder.getPosition() - mPreviousNeo2Rotations / 300 ) ) / 2;
    }
    
    public void handleStopType() {
        if ( hasRun ) {
            setDesiredState( EFourBarState.STOP );
        } else {
            setDesiredState( EFourBarState.NORMAL );
        }
    }

    // later add to setDesiredOutput after states added in
    public void stop() {
        setDesiredOutput( 0, true );
        mNeo1.stopMotor();
        mNeo2.stopMotor();
    }

    public void updateCodex() {
        updateAngularPosition();
        mData.fourbar.set( EFourBarData.A_OUTPUT, -mNeo1.get() );
        mData.fourbar.set( EFourBarData.A_VOLTAGE, mNeo1.getBusVoltage() );
        mData.fourbar.set( EFourBarData.A_CURRENT, mNeo1.getOutputCurrent() );

        mData.fourbar.set( EFourBarData.B_OUTPUT, mNeo2.get() );
        mData.fourbar.set( EFourBarData.B_VOLTAGE, mNeo2.getBusVoltage() );
        mData.fourbar.set( EFourBarData.B_CURRENT, mNeo2.getOutputCurrent() );

        mData.fourbar.set( EFourBarData.ANGLE, mAngularPosition );
    }
}
