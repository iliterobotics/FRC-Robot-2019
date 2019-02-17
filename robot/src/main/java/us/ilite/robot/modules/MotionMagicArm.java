/** 
 * Motion control copied from https://github.com/TripleHelixProgramming/DeepSpace/blob/master/src/main/java/frc/robot/subsystems/JesterWrist.java 
 **/

package us.ilite.robot.modules;

import com.ctre.phoenix.ErrorCode;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import com.team254.lib.drivers.talon.TalonSRXFactory;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import us.ilite.
common.config.SystemSettings;
import us.ilite.common.config.SystemSettings.ArmPosition;
import us.ilite.common.lib.control.PIDController;
import us.ilite.common.lib.control.PIDGains;
import us.ilite.robot.loops.Loop;

import com.team254.lib.util.Util;

public class MotionMagicArm extends Arm
{

    private ILog mLogger = Logger.createLog(this.getClass());


    // Standard wrist positions.  This assumes the pot is mounted such that lower values 
    // correspond to front arm positions.
    // public enum ArmPosAngle {
    //     START(0.0),                             // Wrist starting position
    //     FRONT_LIMIT(ArmPosAngle.START.angle + 135),      // True Limit given to PID control
    //     FRONT(ArmPosAngle.START.angle + 300),           // Normal Position when arm is to the front
    //     TRANSITION(ArmPosAngle.START.angle + 400),      // Position to in when above the highest hatch level
    //     BACK(ArmPosAngle.START.angle + 500),            // Normal Position when arm is to back
    //     BACK_LIMIT(ArmPosAngle.START.angle + 700);      // Ture Limit given to PID Control

    //     public final double angle;

    //     ArmPosAngle(double angle) {
    //         this.angle = angle;
    //     }
    // }

    // private double mVoltage;
    // private double mActualTheta;
    // private double mDesiredTheta;
    // private ESetPoint mDesiredSetPoint;
    // private ESetPoint mCurrentSetPoint;
    // private double kP = 0.002;
    // private double kI = 0.001;
    // private double kD = 0.0;
    // private static final int maxNumTicks = 383; //number of ticks at max arm angle
    // private static final int minNumTicks = 0; //number of ticks at min arm angle
    private TalonSRX talon; //TalonSRXFactory.createDefaultTalon(SystemSettings.kArmTalonSRXAddress);
    // private PIDController pid;
    // private boolean settingPosition;
    private int currentNumTicks = 0; //revisit this and check if correct for encoder type
    private int desiredNumTicks = 0;
    private double mDesiredOutput = 0;
    private boolean stalled = false;
    private boolean motorOff = false; // Motor turned off for a time because of current limiting
    private Timer mTimer;


    // Constants used for translating ticks to angle, values based on ticks per full rotation
    private double tickPerDegree = SystemSettings.kArmPositionEncoderTicksPerRotation / 360.0;
    private double degreePerTick = 360.0 / SystemSettings.kArmPositionEncoderTicksPerRotation;
    
    public MotionMagicArm()
    {
        this.talon = new TalonSRX(SystemSettings.kArmTalonSRXAddress);

        int minTickPosition = this.angleToTicks(ArmPosition.FULLY_DOWN.getAngle());
        int maxTickPosition = this.angleToTicks(ArmPosition.FULLY_UP.getAngle());

        this.currentNumTicks = 0;
        // pid = new PIDController( SystemSettings.kArmPIDGains /*new PIDGains(kP, kI, kD)*/, SystemSettings.kControlLoopPeriod );
        // pid.setInputRange( minTickPosition, maxTickPosition ); //min and max ticks of arm
        if ( talon.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, SystemSettings.kLongCANTimeoutMs) != ErrorCode.OK ) {
            mLogger.error("ArmMotionMagic talon.configSelectedFeedbackSensor error");
        }
        talon.setSelectedSensorPosition(0);
        talon.setStatusFramePeriod(StatusFrameEnhanced.Status_2_Feedback0, 5);

        // init the timer
        this.mTimer = new Timer();
        this.mTimer.reset();

        // Protect the motors and protect from brown out
        talon.configContinuousCurrentLimit(40, 0);
        talon.configPeakCurrentLimit(60, 0);
        talon.configPeakCurrentDuration(100, 0);
        talon.enableCurrentLimit(true);

        talon.setNeutralMode(NeutralMode.Brake);

        talon.selectProfileSlot(0, 0);
        talon.config_kP(0, SystemSettings.kArmPidP, SystemSettings.CTRE_TIMEOUT_INIT);
        talon.config_kI(0, SystemSettings.kArmPidI, SystemSettings.CTRE_TIMEOUT_INIT);
        talon.config_kD(0, SystemSettings.kArmPidD, SystemSettings.CTRE_TIMEOUT_INIT);
        talon.config_kF(0, SystemSettings.kArmPidF, SystemSettings.CTRE_TIMEOUT_INIT);
        
        setArmSoftLimits(minTickPosition, maxTickPosition);
        setArmMotionProfile(SystemSettings.K_ARM_ACCELERATION, SystemSettings.K_ARM_CRUISE);

        talon.configAllowableClosedloopError(0, 2, SystemSettings.CTRE_TIMEOUT_INIT);
    }

    public MotionMagicArm( TalonSRX talon )
    {
        this.talon = talon;
        talon.setInverted(true);
        
        int minTickPosition = this.angleToTicks(ArmPosition.FULLY_DOWN.getAngle());
        int maxTickPosition = this.angleToTicks(ArmPosition.FULLY_UP.getAngle());

        this.currentNumTicks = 0;
        // pid = new PIDController( SystemSettings.kArmPIDGains /*new PIDGains(kP, kI, kD)*/, SystemSettings.kControlLoopPeriod );
        // pid.setInputRange( minTickPosition, maxTickPosition ); //min and max ticks of arm
        if ( talon.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, SystemSettings.kLongCANTimeoutMs) != ErrorCode.OK ) {
            mLogger.error("ArmMotionMagic talon.configSelectedFeedbackSensor error");
        }
        talon.setSelectedSensorPosition(0);
        talon.setStatusFramePeriod(StatusFrameEnhanced.Status_2_Feedback0, 5);

        // init the timer
        this.mTimer = new Timer();
        this.mTimer.reset();

        // Protect the motors and protect from brown out
        talon.configContinuousCurrentLimit(40, 0);
        talon.configPeakCurrentLimit(60, 0);
        talon.configPeakCurrentDuration(100, 0);
        talon.enableCurrentLimit(true);

        talon.setNeutralMode(NeutralMode.Brake);

        talon.selectProfileSlot(0, 0);
        talon.config_kP(0, SystemSettings.kArmPidP, SystemSettings.CTRE_TIMEOUT_INIT);
        talon.config_kI(0, SystemSettings.kArmPidI, SystemSettings.CTRE_TIMEOUT_INIT);
        talon.config_kD(0, SystemSettings.kArmPidD, SystemSettings.CTRE_TIMEOUT_INIT);
        talon.config_kF(0, SystemSettings.kArmPidF, SystemSettings.CTRE_TIMEOUT_INIT);
        
        setArmSoftLimits(minTickPosition, maxTickPosition);
        setArmMotionProfile(SystemSettings.K_ARM_ACCELERATION, SystemSettings.K_ARM_CRUISE);

        talon.configAllowableClosedloopError(0, 2, SystemSettings.CTRE_TIMEOUT_INIT);
    }

    @Override
    public void modeInit(double pNow)
    {
        // reconfigure on enable
        talon.setSelectedSensorPosition(0);
        talon.config_kP(0, SystemSettings.kArmPidP, SystemSettings.CTRE_TIMEOUT_INIT);
        talon.config_kI(0, SystemSettings.kArmPidI, SystemSettings.CTRE_TIMEOUT_INIT);
        talon.config_kD(0, SystemSettings.kArmPidD, SystemSettings.CTRE_TIMEOUT_INIT);
        talon.config_kF(0, SystemSettings.kArmPidF, SystemSettings.CTRE_TIMEOUT_INIT);
        int minTickPosition = this.angleToTicks(ArmPosition.FULLY_DOWN.getAngle());
        int maxTickPosition = this.angleToTicks(ArmPosition.FULLY_UP.getAngle());
        setArmSoftLimits(minTickPosition, maxTickPosition);
        setArmMotionProfile(SystemSettings.K_ARM_ACCELERATION, SystemSettings.K_ARM_CRUISE);
    }

    @Override
    public void periodicInput(double pNow)
    {
        
    }

    /**
     * Update will check for motor stall, if the motor is stalled a timer will be started.
     * If the timer expires and the motor is stalled, the motor will be stopped and the 
     * timer will be started to measure a cooling off period for the motor.  Once the 
     * cooling off period has expired, the motor will be re-enabled.
     */
    @Override
    public void update(double pNow)
    {
        this.currentNumTicks = talon.getSelectedSensorPosition();
        // System.out.println("Arm.update: talon sensor ticks = " + this.currentNumTicks);
        // talon.set(ControlMode.MotionMagic, desiredNumTicks);

        // Directly control the output 
        // double output = this.mDesiredOutput;

        // Calculate the output to control arm position
         //double output = this.calculateOutput();
        
        // Calculate the current/voltage ratio to detect a motor stall
        double current = talon.getOutputCurrent();
        double voltage = talon.getMotorOutputVoltage();
        double ratio = 0.0;

        // When the motor is not running the voltage is zero and divide by zero 
        // is undefined, we will calculate the ratio when the voltage is above
        // a minimum value
        if ( voltage > SystemSettings.kArmMinMotorStallVoltage ) {
            ratio = current / voltage;
        }

        SmartDashboard.putNumber("MMArmVoltage", voltage);
        SmartDashboard.putNumber("MMArmCurrent", current);
        SmartDashboard.putNumber("MMArmStallRatio", ratio);


        // System.out.println("-----------Current ratio = " + ratio + "------------");

        // debug
        // System.out.println( "Arm.update initial output = " + output );
        //System.out.println( "Arm.update: current = " + current + ", voltage = " + voltage + ", ratio = " + ratio);
        //System.out.println( "Arm.update: motorOff = " + this.motorOff + ", stalled = " + this.stalled);


        // If the motor is off check for completion of the cool off period
        if(this.motorOff)
        {
            // System.out.println("*************** Motor OFF **********************************************");
            if( mTimer.hasPeriodPassed(SystemSettings.kArmMotorOffTimeSec) )
            {
                // Cool Off Period has passed, turn the motor back on
                this.motorOff = false;
                this.mTimer.stop();
                this.mTimer.reset();
            }
            else
            {
                // The motor is off, make sure the output is 0.0
            }
        }
        else 
        {
            // check for stalled motor
            if(ratio > SystemSettings.kArmMaxCurrentVoltRatio)
            {
                // System.err.println("++++++++++++++++++++++++++ Motor STALLED ++++++++++++++++++++++++++++++++++++++");
                // System.out.println( "Arm.update: stalled: " + this.stalled);
                // Motor is stalled, where we stalled already
                if(!this.stalled)
                {
                    // Initial motor stall
                    this.stalled = true;
                    // Start counting stall time
                    mTimer.stop();
                    mTimer.reset();
                    mTimer.start();
                }
                else
                {
                    // Already stalled, check for maximum stall time
                    if( mTimer.hasPeriodPassed(SystemSettings.kArmMaxStallTimeSec) )
                    {
                        // We've exceeded the max stall time, stop the motor
                        // System.out.println( "Arm.update Max stall time exceeded." );

                        // We're stopping the motor so reset the stall flag
                        this.stalled = false;

                        // Setting output to 0.0 stops the motor
                        this.motorOff = true;

                        // Restart the timer to measure the cooling off time after the stall
                        mTimer.stop();
                        mTimer.reset();
                        mTimer.start(); // starting for cool-off period

                    }
                }
            }
            else
            {
                // No longer stalled, clear the flag and reset the timer
                this.stalled = false;
                mTimer.stop();
                mTimer.reset();
            }


        }

        if(motorOff)
        {
            talon.set(ControlMode.PercentOutput, 0);
        }
        else
        {
            talon.set(ControlMode.MotionMagic, this.desiredNumTicks);
        }
        
    }

    @Override
    public void shutdown(double pNow)
    {

    }

    @Override
    public void loop(double pNow) {
        update(pNow);
    }

    public void setArmSoftLimits(int reverseSoftLimit, int forwardSoftLimit) {
        talon.configReverseSoftLimitEnable(true, SystemSettings.CTRE_TIMEOUT_PERIODIC);
        talon.configReverseSoftLimitThreshold(reverseSoftLimit, SystemSettings.CTRE_TIMEOUT_PERIODIC);

        talon.configForwardSoftLimitEnable(true, SystemSettings.CTRE_TIMEOUT_PERIODIC);
        talon.configForwardSoftLimitThreshold(forwardSoftLimit, SystemSettings.CTRE_TIMEOUT_PERIODIC);
    }

    private void setArmMotionProfile(int acceleration, int cruise) {
        talon.configMotionAcceleration(acceleration, SystemSettings.CTRE_TIMEOUT_INIT);
        talon.configMotionCruiseVelocity(cruise, SystemSettings.CTRE_TIMEOUT_INIT);
    }

    // public int getSetPoints(ESetPoint pPoint)
    // {
    //     switch( pPoint )
    //     {
    //         case FULLY_OUT:
    //         //90 degrees
    //         return angleToTicks(90.0);

    //         case FULLY_UP:
    //         //135 degrees
    //         return angleToTicks(135.0);

    //         case FULLY_DOWN: // 0 deg
    //         return angleToTicks(0.0);

    //         case MANUAL_STATE:
    //         default:
    //         return 0;
    //     }
    // }


    // private double calculateOutput() //not running on desired output - check later
    // {
    //     pid.setSetpoint(this.desiredNumTicks);
    //     double tempOutput = pid.calculate( talon.getSelectedSensorPosition(), Timer.getFPGATimestamp() );

    //     // Constrain output to min/max values for the Talon
    //     // return Util.limit(tempOutput, -1, 1) * .25;
    //     return Util.limit(tempOutput, SystemSettings.kArmPIDOutputMinLimit, SystemSettings.kArmPIDOutputMaxLimit);

    // }

    private int angleToTicks(double angle)
    {
        return (int) (angle * this.tickPerDegree);
    }

    private double ticksToAngle(int numTicks)
    {
        return numTicks * this.degreePerTick;
    }

    /**
     * Choose a predetermined set point.
     */
    // public void setPoint(ESetPoint pDesiredPoint) 
    // {
    //     this.desiredNumTicks = getSetPoints(pDesiredPoint);
    // }

    /**
     * Choose a predefined arm position
     */
    public void setArmPosition( ArmPosition position ) {
        this.setArmAngle(position.getAngle());
    }

    public void setArmAngle( double angle )
    {
        // TODO Parameterize the angle limits
        // Constrain the angle to the allowed values
        // System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ angle = " + angle);
        angle = Util.limit(angle, SystemSettings.kArmMinAngle, SystemSettings.kArmMaxAngle);
        this.desiredNumTicks = angleToTicks( angle );
    }

    public double getCurrentArmAngle()
    {
        return ticksToAngle( this.talon.getSelectedSensorPosition() );
    }

    /**
     * This is used for direct output control, may go away later
     * @param desiredOutput
     */
    public void setDesiredOutput( double desiredOutput )
    {
        this.mDesiredOutput = Util.limit(desiredOutput, -1, 1);
    }

}