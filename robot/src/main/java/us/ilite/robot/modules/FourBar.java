package us.ilite.robot.modules;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import com.revrobotics.*;
import com.team254.lib.util.Util;

import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Timer;
import us.ilite.common.Data;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.types.EFourBarData;
import us.ilite.common.types.sensor.EPowerDistPanel;
import us.ilite.lib.drivers.SparkMaxFactory;
import us.ilite.robot.hardware.SolenoidWrapper;


public class FourBar extends Module {

    private final double kMinOutput = -1.0;
    private final double kMaxOutput = 1.0;
    private final double kHoldThreshold = 10.0;

    private ILog mLog = Logger.createLog( FourBar.class );
    private Data mData;

    private CANSparkMax mNeos;
    private CANSparkMax mNeo2;

    private Solenoid mPusher;
    private SolenoidWrapper mPusherSolenoid;

    private CANEncoder mNeo1Encoder;
    private CANEncoder mNeo2Encoder;

    private CANPIDController mCanController;

    private double mAngularPosition;
    private double mNeoARotations = 0;
    private double mNeoBRotations = 0;
    private double mOutput;

    /**
     * Construct a FourBar with a Data object
     * @param pData a Data object used to log to codex
     */
    public FourBar( Data pData ) {
        // Later: SystemSettings address
        mNeos = SparkMaxFactory.createDefaultSparkMax(SystemSettings.kFourBarNEO1Address, CANSparkMaxLowLevel.MotorType.kBrushless);
        mNeo2 = SparkMaxFactory.createDefaultSparkMax(SystemSettings.kFourBarNEO2Address, CANSparkMaxLowLevel.MotorType.kBrushless);
        mNeo2.follow( mNeos, true );
    
        mPusher = new Solenoid(SystemSettings.kCANAddressPCM, SystemSettings.kFourBarPusherAddress);
        mPusherSolenoid = new SolenoidWrapper( mPusher );
        
        // Connect the NEO's to the encoders
        mNeo1Encoder = mNeos.getEncoder();
        mNeo2Encoder = mNeo2.getEncoder();
        mNeo1Encoder.setPosition(0);
        mNeo1Encoder.setPosition(0);
        mNeo1Encoder.setPositionConversionFactor(-1.0);
        mNeo2Encoder.setPositionConversionFactor(-1.0);
        mCanController = mNeos.getPIDController();

        mNeos.getEncoder().setPosition(0.0);
        mNeo2.getEncoder().setPosition(0.0);

        mNeos.setIdleMode(CANSparkMax.IdleMode.kBrake);
        mNeo2.setIdleMode(CANSparkMax.IdleMode.kBrake);
        mNeos.burnFlash();
        mNeo2.burnFlash();


        updateAngularPosition();
        mData = pData;
    }


    @Override
    public void modeInit( double pNow ) {
        mLog.error( "FourBar Initialized..." );
        mOutput = 0;

        mNeos.setSmartCurrentLimit( 80 );

        mCanController.setP(SystemSettings.kFourBarP);
        mCanController.setI(SystemSettings.kFourBarI);
        mCanController.setD(SystemSettings.kFourBarD);
        mCanController.setFF(SystemSettings.kFourBarF);

//        mCanController.setSmartMotionMaxAccel(SystemSettings.kMaxElevatorUpAcceleration, 0);
//        mCanController.setSmartMotionMinOutputVelocity(SystemSettings.kMinElevatorVelocity, 0);
//        mCanController.setSmartMotionMaxVelocity(SystemSettings.kMaxElevatorVelocity, 0);
//        mCanController.setSmartMotionMinOutputVelocity(0, 0);
//        mCanController.setSmartMotionAllowedClosedLoopError(SystemSettings.kFourBarClosedLoopAllowableError, 0);
    }

    @Override
    public void periodicInput( double pNow ) {
        updateCodex();
    }

    @Override
    public void update( double pNow ) {
        if(Math.abs(mOutput) < 0.02) {
//            System.out.println("POSITION: " + mAngularPosition);
//            mCanController.setReference(mNeo1Encoder.getPosition(), ControlType.kPosition);
            if ( -35 < mAngularPosition && mAngularPosition < 25) {
                mNeos.set( SystemSettings.kFourbarStallPower );
            } else {
                mNeos.set( 0 );
            }
        } else {
            mNeos.set( mOutput );
        }
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
            mOutput = Util.limit(desiredOutput + gravityCompAtPosition(), kMinOutput, kMaxOutput);
        }
    }

    public void retractPusher() {
        mPusherSolenoid.set(false);
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
        mNeoARotations = -mNeo1Encoder.getPosition() + mNeo1Encoder.getPosition();
        mNeoBRotations = mNeo2Encoder.getPosition();
        mAngularPosition = (mNeoARotations + mNeoBRotations) * 360.0 / 300 / 2;
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
        setDesiredOutput( 0, true );;
    }

    /**
     * Update tracked variables in the codex
     */
    public void updateCodex() {
        updateAngularPosition();
        mData.fourbar.set( EFourBarData.DESIRED_OUTPUT, mOutput );
        mData.fourbar.set( EFourBarData.A_TICKS, mNeoARotations );
//        mData.fourbar.set( EFourBarData.A_OUTPUT, mNeos.get() );
//        mData.fourbar.set( EFourBarData.A_VOLTAGE, mNeos.getAppliedOutput() * 12.0 );
        mData.fourbar.set( EFourBarData.A_CURRENT, mNeos.getOutputCurrent() );

        mData.fourbar.set( EFourBarData.B_TICKS, mNeoBRotations);
//        mData.fourbar.set( EFourBarData.B_OUTPUT, mNeo2.get() );
//        mData.fourbar.set( EFourBarData.B_VOLTAGE, mNeo2.getAppliedOutput() * 12.0 );
        mData.fourbar.set( EFourBarData.B_CURRENT, mNeo2.getOutputCurrent() );

        mData.fourbar.set( EFourBarData.ANGLE, mAngularPosition );
    }

    public boolean isCurrentLimiting() {
        return EPowerDistPanel.isAboveCurrentThreshold(SystemSettings.kFourBarWarnCurrentLimitThreshold, mData.pdp, SystemSettings.kFourBarPdpSlots);
    }


}


