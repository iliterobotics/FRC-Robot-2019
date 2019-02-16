package us.ilite.robot.modules;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import com.revrobotics.CANEncoder;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel;
import com.team254.lib.util.Util;

import us.ilite.common.Data;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.lib.control.PIDController;
import us.ilite.common.types.EFourBarData;


public class FourBar extends Module {

    private final double kMinOutput = -1.0;
    private final double kMaxOutput = 1.0;
    private final double kHoldThreshold = 10.0;

    private ILog mLog = Logger.createLog(FourBar.class);
    private Data mData;
    private PIDController mPIDController;

    private CANSparkMax mNeo1;
    private CANSparkMax mNeo2;
    private CANEncoder mNeo1Encoder;
    private CANEncoder mNeo2Encoder;

    private double mAngularPosition;
    private double mPreviousNeo1Rotations;
    private double mPreviousNeo2Rotations;
    private double mCurrentTime;

    private double mCurrentOutput;

    /**
     * Builds a four bar with a Data object for logging
     * @param pData pass in Data object to update FourBar codex
     */
    public FourBar( Data pData ) {
        // Later: SystemSettings address
        mNeo1 = new CANSparkMax(9, CANSparkMaxLowLevel.MotorType.kBrushless);
        mNeo2 = new CANSparkMax(10, CANSparkMaxLowLevel.MotorType.kBrushless);
        mNeo2.follow( mNeo1, true );
    
        // Connect the NEO's to the encoders
        mNeo1Encoder = mNeo1.getEncoder();
        mNeo2Encoder = mNeo2.getEncoder();

        mAngularPosition = ( ( mNeo1Encoder.getPosition() / 300 ) + ( mNeo2Encoder.getPosition() / 300 ) ) / 2;
        mPIDController = new PIDController( SystemSettings.kFourBarAccelerateGains, 0, 135, SystemSettings.kControlLoopPeriod );
        mData = pData;
    }


    @Override
    public void modeInit(double pNow) {
        mLog.error("FourBar Initialized...");
        mCurrentOutput = 0;
        mPreviousNeo1Rotations = mNeo1Encoder.getPosition();
        mPreviousNeo2Rotations = mNeo2Encoder.getPosition();

        mNeo1.setSmartCurrentLimit( 20 );
        mNeo2.setSmartCurrentLimit( 20 );
        mPIDController.setOutputRange( -1, 1 );
    }

    @Override
    public void periodicInput(double pNow) {
        updateCodex();
    }

    @Override
    public void update(double pNow) {
        mCurrentTime = pNow;
        mNeo1.set( mCurrentOutput );
        if ( mAngularPosition >= 135 ) {
            setDesiredState( EFourBarState.STOP );
        }
    }

    /**
     * Sets desired state based on current degrees
     */
    public void handleUpState() {
        setDesiredState( EFourBarState.fromDegrees( mAngularPosition ) );
    }

    /**
     * Handles motor output based on desired state
     * @param desiredState the state used to apply output
     */
    public void setDesiredState( EFourBarState desiredState ) {

        double mOutputToApply = 0.0;

        switch ( desiredState ) {
            case NORMAL:
                // do nothing
                mOutputToApply = 0;
            case STOP:
                // stop climber, cut off power
                mOutputToApply = 0;
                mNeo1.stopMotor();
            case HOLD:
                // hold in place
                mOutputToApply = gravityCompAtPosition();
            case ACCELERATE:
                // apply pid to output on accelerate controller settings
                mPIDController.setPIDGains( SystemSettings.kFourBarAccelerateGains );
                mPIDController.setSetpoint( EFourBarState.ACCELERATE.getUpperAngularBound() );
                mOutputToApply = mPIDController.calculate( mAngularPosition, mCurrentTime ) + gravityCompAtPosition();
            case DECELERATE:
                // apply pid to output on decelerate controller settings
                mPIDController.setPIDGains( SystemSettings.kFourBarDecelerateGains );
                mPIDController.setSetpoint( EFourBarState.DECELERATE.getUpperAngularBound() );
                mOutputToApply = mPIDController.calculate( mAngularPosition, mCurrentTime ) + gravityCompAtPosition();
        }
        mCurrentOutput = Util.limit( mOutputToApply, kMinOutput, kMaxOutput );
    }

    /**
     * Holds in current position
     * Holds in air with gravity comp if four bar is being used
     * If it is not being used -> don't apply any output
     */
    public void handleStopType() {
        if ( Math.abs( mAngularPosition ) >= kHoldThreshold ) {
            setDesiredState( EFourBarState.HOLD );
        } else {
            setDesiredState( EFourBarState.NORMAL );
        }
    }

    /**
     * Get the output for gravity compensation at the current angle
     * @return the percent output to compensate for gravity
     */
    public double gravityCompAtPosition() {
        return SystemSettings.kTFourBar * Math.cos( mAngularPosition );
    }

    /**
     * Updates the angular position based on rotations
     */
    public void updateAngularPosition() {
        mAngularPosition = ( ( mNeo1Encoder.getPosition() - mPreviousNeo1Rotations / 300 ) + ( mNeo2Encoder.getPosition() - mPreviousNeo2Rotations / 300 ) ) / 2;
    }

    public void toNeos(  ) {

    }

    /**
     * Updates all tracking variables to the codex
     */
    public void updateCodex() {
        updateAngularPosition();
        mData.fourbar.set( EFourBarData.A_OUTPUT, mNeo1.get() );
        mData.fourbar.set( EFourBarData.A_VOLTAGE, mNeo1.getBusVoltage() );
        mData.fourbar.set( EFourBarData.A_CURRENT, mNeo1.getOutputCurrent() );

        mData.fourbar.set( EFourBarData.B_OUTPUT, mNeo2.get() );
        mData.fourbar.set( EFourBarData.B_VOLTAGE, mNeo2.getBusVoltage() );
        mData.fourbar.set( EFourBarData.B_CURRENT, mNeo2.getOutputCurrent() );

        mData.fourbar.set( EFourBarData.ANGLE, mAngularPosition );
    }

    @Override
    public void shutdown(double pNow) {
        mNeo1.disable();
    }
}
