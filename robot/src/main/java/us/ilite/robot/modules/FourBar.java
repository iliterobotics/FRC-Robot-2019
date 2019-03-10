package us.ilite.robot.modules;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import com.revrobotics.CANEncoder;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel;
import com.team254.lib.util.Util;

import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Timer;
import us.ilite.common.Data;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.types.EFourBarData;
import us.ilite.lib.drivers.SparkMaxFactory;


public class FourBar extends Module {

    private final double kMinOutput = -1.0;
    private final double kMaxOutput = 1.0;
    private final double kHoldThreshold = 10.0;

    private ILog mLog = Logger.createLog( FourBar.class );
    private Data mData;

    private CANSparkMax mNeos;
    private CANSparkMax mNeo2;
    private Solenoid mPusherSolenoid;
    private boolean mHasPusherActivated = false;
    private Timer mPusherSolenoidTimer = new Timer();
    private CANEncoder mNeo1Encoder;
    private CANEncoder mNeo2Encoder;

    private double mAngularPosition;
    private double mPreviousNeo1Rotations;
    private double mPreviousNeo2Rotations;

    private double mOutput;

    /**
     * Construct a FourBar with a Data object
     * @param pData a Data object used to log to codex
     */
    public FourBar( Data pData ) {
        // Later: SystemSettings address
        mNeos = SparkMaxFactory.createDefaultSparkMax(SystemSettings.kFourBarNEO1Address, CANSparkMaxLowLevel.MotorType.kBrushless);
        mNeo2 = SparkMaxFactory.createDefaultSparkMax(SystemSettings.kFourBarNEO2Address, CANSparkMaxLowLevel.MotorType.kBrushless);
        mPusherSolenoid = new Solenoid(SystemSettings.kCANAddressPCM, SystemSettings.kFourBarPusherAddress);
        mNeo2.follow( mNeos, true );
    
        // Connect the NEO's to the encoders
        mNeo1Encoder = mNeos.getEncoder();
        mNeo2Encoder = mNeo2.getEncoder();

        updateAngularPosition();
        mData = pData;
    }


    @Override
    public void modeInit( double pNow ) {
        mLog.error( "FourBar Initialized..." );
        mOutput = 0;
        mPreviousNeo1Rotations = mNeo1Encoder.getPosition();
        mPreviousNeo2Rotations = mNeo2Encoder.getPosition();

        mNeos.setSmartCurrentLimit( 80 );
    }

    @Override
    public void periodicInput( double pNow ) {
        updateCodex();
    }

    @Override
    public void update( double pNow ) {
        mNeos.set( mOutput );
    }

    @Override
    public void shutdown( double pNow ) {
        mNeos.disable();
    }

    /**
     * Sets output to desired output
     * @param desiredOutput the desired percent output
     * @param isIdle whether it is idle or not ( don't add gravity comp if just b is being held )
     */
    public void setDesiredOutput( double desiredOutput, boolean isIdle ) {
        if ( isIdle ) {
            mOutput = 0;
        } else {

//            if(!mHasPusherActivated) {
//                mPusherSolenoidTimer.reset();
//                mPusherSolenoidTimer.start();
//                mHasPusherActivated = true;
//                mPusherSolenoid.set(true);
//            }
//
//            if(mPusherSolenoidTimer.hasPeriodPassed(SystemSettings.kFourBarPusherDelay)) {
                mOutput = Util.limit(desiredOutput + gravityCompAtPosition(), kMinOutput, kMaxOutput);
//            }
        }
    }

    public void retractPusher() {
        mPusherSolenoid.set(false);
//        mHasPusherActivated = false;
//        mPusherSolenoidTimer.stop();
//        mPusherSolenoidTimer.reset();
    }

    public void extendPusher() {
        mPusherSolenoid.set(true);
    }

    /**
     * Calculates necessary output to counter gravity
     * @return the percent output to counter gravity
     */
    public double gravityCompAtPosition() {
        return SystemSettings.kTFourBar * Math.cos( mAngularPosition );
    }

    /**
     * Update angular position based on current rotations
     */
    public void updateAngularPosition() {
        mAngularPosition = (-mNeo1Encoder.getPosition() + mNeo2Encoder.getPosition()) / 2;
    }
    
    /**
     * Handle stop type based on location
     * Hold if not at 0
     */
    public void handleStopType() {
        if ( Math.abs( mAngularPosition ) > kHoldThreshold ) {
            hold();
        } else {
            stop();
        }
    }

    /**
     * holds arm at current location using gravity compensation
     */
    public void hold() {
        mOutput = gravityCompAtPosition();
    }

    /**
     * Cut power to the motor
     */
    public void stop() {
        setDesiredOutput( 0, true );
        mNeos.stopMotor();
    }

    /**
     * Update tracked variables in the codex
     */
    public void updateCodex() {
        updateAngularPosition();
        mData.fourbar.set( EFourBarData.A_TICKS, mNeos.getEncoder().getPosition() );
        mData.fourbar.set( EFourBarData.A_OUTPUT, mNeos.get() );
        mData.fourbar.set( EFourBarData.A_VOLTAGE, mNeos.getAppliedOutput() * 12.0 );
        mData.fourbar.set( EFourBarData.A_CURRENT, mNeos.getOutputCurrent() );

        mData.fourbar.set( EFourBarData.B_TICKS, mNeo2.getEncoder().getPosition() );
        mData.fourbar.set( EFourBarData.B_OUTPUT, mNeo2.get() );
        mData.fourbar.set( EFourBarData.B_VOLTAGE, mNeo2.getAppliedOutput() * 12.0 );
        mData.fourbar.set( EFourBarData.B_CURRENT, mNeo2.getOutputCurrent() );

        mData.fourbar.set( EFourBarData.ANGLE, mAngularPosition );
    }
}
