//=============================================================================//
//                                                                             //
//              Modified class originally written by Team 254                  //
//                                                                             //
//=============================================================================//
package us.ilite.common.lib.control;

import com.flybotix.hfr.codex.Codex;
import com.flybotix.hfr.codex.CodexOf;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

import com.team254.lib.util.Util;

public class PIDController {

    private ILog mLogger = Logger.createLog( this.getClass() );

    private Codex<Double, EPIDController> mPIDControl = Codex.of.thisEnum( EPIDController.class );

    private boolean mContinuous = false;

    private double mPreviousTime;
    private PIDGains mPIDGains;
    private double mDt;
    private double mMaximumOutput = 1.0;
    private double mMinimumOutput = -1.0;
    private double mMaximumInput = 0.0;
    private double mMinimumInput = 0.0;

    private double mPrevError = 0.0;
    private double mTotalError = 0.0;
    private double mSetpoint = 0.0;
    private double mError = 100.0;
    private double mResult = 0.0;
    private double mLastInput = Double.NaN;
    private double mDeadband = 0.0; // If the absolute error is less than
                                     // deadband
                                     // then treat error for the proportional
                                     // term as 0
    private double mInputForCodex;
    private double mOutputForCodex;
    private double mDTForCodex;
    private double mDefaultDT;


    /**
     * Constructs a PIDController object with a PIDGains object and defaultDT
     * @param kPIDGains PIDGains object holding PIDF values
     * @param KdefaultDT the default delta time ( SystemSettings.kControlLoopPeriod )
     */
    public PIDController( PIDGains kPIDGains, double kDefaultDT ) {
        mPIDGains = kPIDGains;
        mDefaultDT = kDefaultDT;
    }

    /**
     * Calculating output based on pid constants
     * @param input the current position
     * @param absoluteTime the current time ( pNow ) 
     * @return the output to apply
     */
    public double calculate( double input, double absoluteTime ) {
        mInputForCodex = input;
        if ( mDt == 0.0 ) {
            mDt = mDefaultDT;
        } else {
            mDt = absoluteTime - mPreviousTime;
        }
        mDTForCodex = mDt;
        mLastInput = input;
        mError = mSetpoint - input;
        

        // Error continuity for rotational pid
        if ( mContinuous ) {
            if ( Math.abs( mError ) > ( mMaximumInput - mMinimumInput ) / 2 ) {
                if ( mError > 0 ) {
                    mError = mError - mMaximumInput + mMinimumInput;
                } else {
                    mError = mError + mMaximumInput - mMinimumInput;
                }
            }
        }

        // Only add to totalError if output isn't being saturated
        if ( ( mError * mPIDGains.kP < mMaximumOutput ) && ( mError * mPIDGains.kP > mMinimumOutput ) ) {
            mTotalError += mError * mDt;
        } else {
            mTotalError = 0;
        }

        // Don't blow away mError so as to not break derivative
        double proportionalError = Math.abs( mError ) < mDeadband ? 0 : mError;

        mResult = (mPIDGains.kP * proportionalError) + (mPIDGains.kI * mTotalError) + (mPIDGains.kD * ( mError - mPrevError ) / mDt)
                + (mPIDGains.kF * mSetpoint );
        mPrevError = mError;

        mResult = Util.limit( mResult, mMaximumOutput );
        mPreviousTime = absoluteTime;

        mOutputForCodex = mResult;
        logToCodex();
        return mResult;
    }

    /**
     * Determines if the error is within a certain threshold
     * @param tolerance the threshold to check if error is within
     * @return true when error is within -tolerance and tolerance
     */
    public boolean isOnTarget( double tolerance ) {
        return mLastInput != Double.NaN && Math.abs( mLastInput - mSetpoint ) < tolerance;
    }

    /**
     * Resets the input, previous error, total error, calculate() output, and setpoint
     */
    public void reset() {
        mLastInput = Double.NaN;
        mPrevError = 0;
        mTotalError = 0;
        mResult = 0;
        mSetpoint = 0;
    }

    /**
     * resets total error
     */
    public void resetIntegrator() {
        mTotalError = 0;
    }

    public void logToCodex() {
        mPIDControl.set( EPIDController.CURRENT, mInputForCodex );
        mPIDControl.set( EPIDController.OUTPUT, mOutputForCodex );
        mPIDControl.set( EPIDController.GOAL, mSetpoint );
        mPIDControl.set( EPIDController.ERROR, mError );
        mPIDControl.set( EPIDController.DELTA_TIME, mDTForCodex );
        mPIDControl.set( EPIDController.P_GAIN, mPIDGains.kP );
        mPIDControl.set( EPIDController.I_GAIN, mPIDGains.kI );
        mPIDControl.set( EPIDController.D_GAIN, mPIDGains.kD );
        mPIDControl.set( EPIDController.F_GAIN, mPIDGains.kF );
    }

    enum EPIDController implements CodexOf<Double> {

        ERROR, OUTPUT, CURRENT, GOAL,
        P_GAIN, I_GAIN, D_GAIN, F_GAIN, DELTA_TIME;
    }

    // ####### //
    // Setters //
    // ####### //
    /**
     * Sets the input ( Starting distance ) range
     * @param minimumInput the minimum input
     * @param maximumInput the maximum input
     */
    public void setInputRange( double minimumInput, double maximumInput ) {
        if ( minimumInput > maximumInput ) {
            mLogger.debug( "Lower bound is greater than upper bound" );
        }
        mMinimumInput = minimumInput;
        mMaximumInput = maximumInput;
        setSetpoint( mSetpoint );
    }
    
    /**
     * Sets the ( pid calculation ) output range
     * @param minimumOutput the minimum output
     * @param maximumOutput the maximum output
     */
    public void setOutputRange( double minimumOutput, double maximumOutput ) {
        if ( minimumOutput > maximumOutput ) {
            mLogger.debug( "Lower bound is greater than upper bound" );
        }
        mMinimumOutput = minimumOutput;
        mMaximumOutput = maximumOutput;
    }

    public void setSetpoint( double setpoint ) {
        mSetpoint = Util.limit( setpoint, mMinimumInput, mMaximumInput );
    }

    /**
     * Enables or disables continuous for rotational pid
     * @param continuous true to enable continuous, false to disable continuous
     */
    public void setContinuous( boolean continuous ) {
        mContinuous = continuous;
    }

    public void setPIDGains( PIDGains newPIDGains ) {
        mPIDGains = newPIDGains;
        logToCodex();
    }

    public void setDeadband( double deadband ) {
        mDeadband = deadband;
    }

    // ####### //
    // Getters //
    // ####### //
    /**
     * Access the codex holding PIDController values
     * @return the codex holding PIDController values
     */
    public Codex<Double, EPIDController> getCodex() {
        return mPIDControl;
    }

    public PIDGains getPIDGains() {
        return mPIDGains;
    }

    public double getOutput() {
        return mResult;
    }

    public double getSetpoint() {
        return mSetpoint;
    }

    public double getError() {
        return mError;
    }
}