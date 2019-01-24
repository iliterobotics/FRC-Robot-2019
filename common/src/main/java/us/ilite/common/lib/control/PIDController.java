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
/**
 * This class implements a PID Control Loop.
 * 
 * Does all computation synchronously (i.e. the calculate() function must be called by the user from his own thread)
 */
public class PIDController {

    private ILog mLogger = Logger.createLog(this.getClass());

    private Codex<Double, EPIDControl> mPIDControl = Codex.of.thisEnum(EPIDControl.class);

    private double m_previousTime;
    private double m_defaultDT;
    private double m_dt;

    private double m_P; // factor for "proportional" control
    private double m_I; // factor for "integral" control
    private double m_D; // factor for "derivative" control
    private double m_F; // factor for feed forward gain
    private double m_maximumOutput = 1.0; // |maximum output|
    private double m_minimumOutput = -1.0; // |minimum output|
    private double m_maximumInput = 0.0; // maximum input - limit setpoint to
                                         // this
    private double m_minimumInput = 0.0; // minimum input - limit setpoint to
                                         // this
    private double m_inputForCodex;
    private boolean m_continuous = false; // do the endpoints wrap around? eg.
                                          // Absolute encoder
    private double m_prevError = 0.0; // the prior sensor input (used to compute
                                      // velocity)
    private double m_totalError = 0.0; // the sum of the errors for use in the
                                       // integral calc
    private double m_setpoint = 0.0;
    private double m_error = 100.0;
    private double m_result = 0.0;
    private double m_last_input = Double.NaN;
    private double m_deadband = 0.0; // If the absolute error is less than
                                     // deadband
                                     // then treat error for the proportional
                                     // term as 0

    public PIDController(double Kp, double Ki, double Kd, double KdefaultDT) {
        this( Kp, Ki, Kd, 0d, KdefaultDT );
    }

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
        if ((m_error * m_P < m_maximumOutput) && (m_error * m_P > m_minimumOutput)) {
            m_totalError += m_error * m_dt;
        } else {
            m_totalError = 0;
        }

        // Don't blow away m_error so as to not break derivative
        double proportionalError = Math.abs(m_error) < m_deadband ? 0 : m_error;

        m_result = (m_P * proportionalError + m_I * m_totalError + m_D * (m_error - m_prevError) / m_dt
                + m_F * m_setpoint);
        m_prevError = m_error;

        m_result = Util.limit( m_result, m_maximumOutput );
        m_previousTime = absoluteTime;

        logToCodex();
        return m_result;
    }

    public void setPID(double p, double i, double d) {
        setPID( p, i, d, 0d );
    }

    public void setPID(double p, double i, double d, double f) {
        m_P = p;
        m_I = i;
        m_D = d;
        m_F = f;
        logToCodex();
    }

    public double getP() {
        return m_P;
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

    /**
     * Enables or disables continuous for rotational pid
     * @param continuous true to enable continuous, false to disable continuous
     */
    public void setContinuous(boolean continuous) {
        m_continuous = continuous;
    }

    public void setDeadband(double deadband) {
        m_deadband = deadband;
    }

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

    public double getSetpoint() {
        return m_setpoint;
    }

    public double getError() {
        return m_error;
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

    public void resetIntegrator() {
        m_totalError = 0;
    }

    public void logToCodex() {
        mPIDControl.set( EPIDControl.OUTPUT, m_P );
        mPIDControl.set( EPIDControl.ERROR, m_error );
        mPIDControl.set( EPIDControl.CURRENT, m_inputForCodex );
        mPIDControl.set( EPIDControl.P_GAIN, m_P );
        mPIDControl.set( EPIDControl.I_GAIN, m_I );
        mPIDControl.set( EPIDControl.D_GAIN, m_D );
        mPIDControl.set( EPIDControl.F_GAIN, m_F );
    }

    /**
     * Access the codex holding PIDController values
     * @return the codex holding PIDController values
     */
    public Codex<Double, EPIDControl> getCodex() {
        return mPIDControl;
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
}