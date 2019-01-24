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

    private ILog mLogger = Logger.createLog(this.getClass());

    private Codex<Double, EPIDControl> mPIDControl = Codex.of.thisEnum(EPIDControl.class);

    private boolean m_continuous = false;

    private double m_previousTime;
    private PIDGains mPIDGains;
    private double m_I;
    private double m_D;
    private double m_F;
    private double m_dt;
    private double m_maximumOutput = 1.0;
    private double m_minimumOutput = -1.0;
    private double m_maximumInput = 0.0;
    private double m_minimumInput = 0.0;

    private double m_prevError = 0.0;
    private double m_totalError = 0.0;
    private double m_setpoint = 0.0;
    private double m_error = 100.0;
    private double m_result = 0.0;
    private double m_last_input = Double.NaN;
    private double m_deadband = 0.0; // If the absolute error is less than
                                     // deadband
                                     // then treat error for the proportional
                                     // term as 0
    private double m_inputForCodex;
    private double m_defaultDT;


    /**
     * Constructs a PIDController object with P, I, D and defaultDT
     * @param Kp the proportional constant
     * @param Ki the integral constant
     * @param Kd the derivative constant
     * @param KdefaultDT the default delta time (SystemSettings.kControlLoopPeriod)
     */
    public PIDController(PIDGains kPIDGains, double kDefaultDT) {
        mPIDGains = kPIDGains;
        mDefaultDT = kDefaultDT;
    }

    /**
     * Constructs a PIDController object with P, I, D, F and defaultDT
     * @param Kp the proportional constant
     * @param Ki the integral constant
     * @param Kd the derivative constant
     * @param Kf the feed forward constant
     * @param KdefaultDT the default delta time (SystemSettings.kControlLoopPeriod)
     */
    public PIDController(double Kp, double Ki, double Kd, double Kf, double KdefaultDT) {
        m_P = Kp;
        m_I = Ki;
        m_D = Kd;
        m_F = Kf;
        m_defaultDT = KdefaultDT;
        logToCodex();
    }

    /**
     * Calculating output based on pid constants
     * @param input the current position
     * @param absoluteTime the current time (pNow) 
     * @return the output to apply
     */
    public double calculate(double input, double absoluteTime) {
        m_inputForCodex = input;
        if ( m_dt == 0.0 ) {
            m_dt = m_defaultDT;
        } else {
            m_dt = absoluteTime - m_previousTime;
        }
        m_last_input = input;
        m_error = m_setpoint - input;
        

        // Error continuity for rotational pid
        if (m_continuous) {
            if (Math.abs(m_error) > (m_maximumInput - m_minimumInput) / 2) {
                if (m_error > 0) {
                    m_error = m_error - m_maximumInput + m_minimumInput;
                } else {
                    m_error = m_error + m_maximumInput - m_minimumInput;
                }
            }
        }

        // Only add to totalError if output isn't being saturated
        if ((mError * mPIDGains.mP < mMaximumOutput) && (mError * mPIDGains.mP > mMinimumOutput)) {
            m_totalError += m_error * m_dt;
        } else {
            m_totalError = 0;
        }

        // Don't blow away m_error so as to not break derivative
        double proportionalError = Math.abs(m_error) < m_deadband ? 0 : m_error;

        mResult = (mPIDGains.mP * proportionalError + mPIDGains.mI * mTotalError + mPIDGains.mD * (mError - mPrevError) / mDt
                + mPIDGains.mF * mSetpoint);
        mPrevError = mError;

        m_result = Util.limit( m_result, m_maximumOutput );
        m_previousTime = absoluteTime;

        logToCodex();
        return m_result;
    }

    /**
     * Determines if the error is within a certain threshold
     * @param tolerance the threshold to check if error is within
     * @return true when error is within -tolerance and tolerance
     */
    public boolean isOnTarget(double tolerance) {
        return m_last_input != Double.NaN && Math.abs(m_last_input - m_setpoint) < tolerance;
    }

    /**
     * Resets the input, previous error, total error, calculate() output, and setpoint
     */
    public void reset() {
        m_last_input = Double.NaN;
        m_prevError = 0;
        m_totalError = 0;
        m_result = 0;
        m_setpoint = 0;
    }

    /**
     * resets total error
     */
    public void resetIntegrator() {
        m_totalError = 0;
    }

    public void logToCodex() {
        mPIDControl.set( EPIDControl.OUTPUT, m_P );
        mPIDControl.set( EPIDControl.ERROR, m_error );
        mPIDControl.set( EPIDControl.CURRENT, m_inputForCodex );
        mPIDControl.set( EPIDControl.P_GAIN, m_P );
        mPIDControl.set( EPIDControl.P_GAIN, mPIDGains.mP );
        mPIDControl.set( EPIDControl.I_GAIN, mPIDGains.mI );
        mPIDControl.set( EPIDControl.D_GAIN, mPIDGains.mD );
        mPIDControl.set( EPIDControl.F_GAIN, mPIDGains.mF );
    }

    enum EPIDControl implements CodexOf<Double> {
        ERROR,
        OUTPUT,
        CURRENT,

        P_GAIN,
        I_GAIN,
        D_GAIN,
        F_GAIN;
    }

    // ####### //
    // Setters //
    // ####### //
    /**
     * Sets the input (Starting distance) range
     * @param minimumInput the minimum input
     * @param maximumInput the maximum input
     */
    public void setInputRange(double minimumInput, double maximumInput) {
        if (minimumInput > maximumInput) {
            mLogger.debug("Lower bound is greater than upper bound");
        }
        m_minimumInput = minimumInput;
        m_maximumInput = maximumInput;
        setSetpoint(m_setpoint);
    }
    
    /**
     * Sets the (pid calculation) output range
     * @param minimumOutput the minimum output
     * @param maximumOutput the maximum output
     */
    public void setOutputRange(double minimumOutput, double maximumOutput) {
        if (minimumOutput > maximumOutput) {
            mLogger.debug("Lower bound is greater than upper bound");
        }
        m_minimumOutput = minimumOutput;
        m_maximumOutput = maximumOutput;
    }

    public void setSetpoint(double setpoint) {
        m_setpoint = Util.limit(setpoint, m_minimumInput, m_maximumInput);
    }

    /**
     * Enables or disables continuous for rotational pid
     * @param continuous true to enable continuous, false to disable continuous
     */
    public void setContinuous(boolean continuous) {
        m_continuous = continuous;
    }

    public void setPID(double p, double i, double d) {
        setPID( p, i, d, 0d );
    }

    public void setPIDGains( PIDGains newPIDGains ) {
        mPIDGains = newPIDGains;
        m_I = i;
        m_D = d;
        m_F = f;
        logToCodex();
    }

    public void setDeadband(double deadband) {
        m_deadband = deadband;
    }

    // ####### //
    // Getters //
    // ####### //
    /**
     * Access the codex holding PIDController values
     * @return the codex holding PIDController values
     */
    public Codex<Double, EPIDControl> getCodex() {
        return mPIDControl;
    }

    public PIDGains getPIDGains() {
        return mPIDGains;
    }

    public double getI() {
        return m_I;
    }

    public double getD() {
        return m_D;
    }

    public double getF() {
        return m_F;
    }

    public double getOutput() {
        return m_result;
    }

    public double getSetpoint() {
        return m_setpoint;
    }

    public double getError() {
        return m_error;
    }
}