package us.ilite.robot.modules;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import com.team254.lib.util.Util;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.config.SystemSettings.ArmPosition;
import us.ilite.common.lib.control.PIDController;

public class BasicArm extends Arm {

    private ILog mLogger = Logger.createLog(this.getClass());

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
    private TalonSRX talon = new TalonSRX(SystemSettings.kArmTalonSRXAddress); //TalonSRXFactory.createDefaultTalon(SystemSettings.kArmTalonSRXAddress);
    private PIDController pid;
    // private boolean settingPosition;
    private int currentNumTicks = 0; // Our current angle in ticks
    private int desiredNumTicks = 0; // Our desired angle in ticks
    private double mDesiredOutput; // This is used for direct driving the arm
    private boolean stalled = false;
    private boolean motorOff = false; // Motor turned off for a time because of current limiting
    private Timer mTimer;

    public double maxOutputSeen = 0.0;
    public double minOutputSeen = 0.0;

    // Constants used for translating ticks to angle, values based on ticks per full rotation
    private double tickPerDegree = SystemSettings.kArmPositionEncoderTicksPerRotation / 360.0;
    private double degreePerTick = 360.0 / SystemSettings.kArmPositionEncoderTicksPerRotation;

    public BasicArm()
    {
        int minTickPosition = this.angleToTicks(ArmPosition.FULLY_DOWN.getAngle());
        int maxTickPosition = this.angleToTicks(ArmPosition.FULLY_UP.getAngle());

        this.currentNumTicks = 0;
        pid = new PIDController( SystemSettings.kArmPIDGains /*new PIDGains(kP, kI, kD)*/, minTickPosition, maxTickPosition, SystemSettings.kControlLoopPeriod );
        talon.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, SystemSettings.kLongCANTimeoutMs);
        talon.setSelectedSensorPosition(0);
        talon.setStatusFramePeriod(StatusFrameEnhanced.Status_2_Feedback0, 5);

        // init the timer
        this.mTimer = new Timer();
        this.mTimer.reset();
    }

    public BasicArm(TalonSRX talon)
    {
        this.talon = talon;
        
        int minTickPosition = this.angleToTicks(ArmPosition.FULLY_DOWN.getAngle());
        int maxTickPosition = this.angleToTicks(ArmPosition.FULLY_UP.getAngle());

        this.currentNumTicks = 0;
        pid = new PIDController( SystemSettings.kArmPIDGains /*new PIDGains(kP, kI, kD)*/, minTickPosition, maxTickPosition, SystemSettings.kControlLoopPeriod );
        talon.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, SystemSettings.kLongCANTimeoutMs);
        talon.setSelectedSensorPosition(0);
        talon.setStatusFramePeriod(StatusFrameEnhanced.Status_2_Feedback0, 5);

        // init the timer
        this.mTimer = new Timer();
        this.mTimer.reset();
    }

    @Override
    public void modeInit(double pNow)
    {
        // rezero on enable
        int minTickPosition = this.angleToTicks(ArmPosition.FULLY_DOWN.getAngle());
        int maxTickPosition = this.angleToTicks(ArmPosition.FULLY_UP.getAngle());

        this.currentNumTicks = 0;
        pid.setPIDGains(SystemSettings.kArmPIDGains);
        pid.setInputRange( minTickPosition, maxTickPosition ); //min and max ticks of arm

        talon.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, SystemSettings.kLongCANTimeoutMs);
        talon.setSelectedSensorPosition(0);
        talon.setStatusFramePeriod(StatusFrameEnhanced.Status_2_Feedback0, 5);

        // init the timer
        this.mTimer.stop();
        this.mTimer.reset();

        // for debug
        maxOutputSeen = 0.0;
        minOutputSeen = 0.0;
    
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
     * 
     * We will adjust the PID Gain to add additional damping as we get with in the landing range
     * of the desired target. See SystemSettings.kArmLandingRangeAngle.
     */
    @Override
    public void update(double pNow)
    {
        this.currentNumTicks = talon.getSelectedSensorPosition();

        // Check to see if we are with in the landing range
        double deltaAngle = this.ticksToAngle(Math.abs(this.desiredNumTicks - this.currentNumTicks));
        if ( deltaAngle <= SystemSettings.kArmLandingRangeAngle ) {
            // We are with in the landing range, use the landing PID Gain values'
            pid.setPIDGains(SystemSettings.kArmLandingPIDGains);
        }
        else {
            // use the normal PID gains
            pid.setPIDGains(SystemSettings.kArmPIDGains);
        }

        // System.out.println("Arm.update: talon sensor ticks = " + this.currentNumTicks);

        // Directly control the output 
        // double output = this.mDesiredOutput;

        // Calculate the output to control arm position
         double output = this.calculateOutput();

         // Add the gravity compensation
         output += SystemSettings.kArmKg * Math.cos( this.ticksToAngle(this.currentNumTicks) - 90.0 );
        
        // Calculate the current/voltage ratio to detect a motor stall
        double current = talon.getOutputCurrent();
        double voltage = talon.getMotorOutputVoltage();
        double ratio = 0.0;
        // System.out.println("-----------Current ratio = " + ratio + "------------");


        // When the motor is not running the voltage is zero and divide by zero 
        // is undefined, we will calculate the ratio when the voltage is above
        // a minimum value
        if ( voltage > SystemSettings.kArmMinMotorStallVoltage ) {
            ratio = current / voltage;
        }

        SmartDashboard.putNumber("BasicArmVoltage", voltage);
        SmartDashboard.putNumber("BasicArmCurrent", current);
        SmartDashboard.putNumber("BasicArmStallRatio", ratio);
        SmartDashboard.putNumber("BasicArmCurrentAngle", this.ticksToAngle(this.currentNumTicks));
        SmartDashboard.putNumber("BasicArmDesiredAngle", this.ticksToAngle(this.desiredNumTicks));
        SmartDashboard.putNumber("BasicArmDesiredAngleDelta", this.ticksToAngle(this.desiredNumTicks - this.currentNumTicks));
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
                output = 0.0;
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
                        output = 0.0;
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

        // System.out.println( "Arm.update: About to set talon output )))))))))))))))))))))))))))))))))))))))= " + output );

        if ( output < minOutputSeen ) {
            minOutputSeen = output;
        }

        if ( output > maxOutputSeen) {
            maxOutputSeen = output;
        }

        // System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ min = " + minOutputSeen + " max= " + maxOutputSeen);

        SmartDashboard.putNumber("BasicArmSetOutput", output);
        talon.set(ControlMode.PercentOutput, output);
        
    }

    @Override
    public void shutdown(double pNow)
    {

    }

    @Override
    public void loop(double pNow) {
        update(pNow);
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


    private double calculateOutput() //not running on desired output - check later
    {
        pid.setSetpoint(this.desiredNumTicks);
        double tempOutput = pid.calculate( talon.getSelectedSensorPosition(), Timer.getFPGATimestamp() );

        // Constrain output to min/max values for the Talon
        return Util.limit(tempOutput, SystemSettings.kArmPIDOutputMinLimit, SystemSettings.kArmPIDOutputMaxLimit);

    }

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
        SmartDashboard.putNumber("ArmCommandedAngle", angle);

        angle = Util.limit(angle, SystemSettings.kArmMinAngle, SystemSettings.kArmMaxAngle);
        // this.desiredNumTicks = angleToTicks( angle );
        this.desiredNumTicks = angleToTicks( angle );
        // System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@!!!!! angle = " + angle);
        // System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ ticks = " + this.desiredNumTicks);
        // System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ min = " + minOutputSeen + " max= " + maxOutputSeen);
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