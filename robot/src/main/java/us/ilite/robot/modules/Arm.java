package us.ilite.robot.modules;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.team254.lib.drivers.talon.TalonSRXFactory;

import edu.wpi.first.wpilibj.Timer;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.lib.control.PIDController;
import us.ilite.common.lib.control.PIDGains;
import com.team254.lib.util.Util;

public class Arm extends Module
{
    private static final double MAX_CURRENT_V_RATIO = 1; //tune - overcurrent ratio for arm motor
    private static final double MOTOR_OFF_TIME = 0.5;
    private static final double MAX_STALL_TIME = 0.1;
    // private double mVoltage;
    // private double mActualTheta;
    // private double mDesiredTheta;
    // private ESetPoint mDesiredSetPoint;
    // private ESetPoint mCurrentSetPoint;
    private double kP = 0.0;
    private double kI = 0.0;
    private double kD = 0.0;
    private static final int maxNumTicks = 383; //number of ticks at max arm angle
    private static final int minNumTicks = 0; //number of ticks at min arm angle
    private static int talonPortId = 6; //placeholder constant; change later in SystemSettings
    private TalonSRX talon = TalonSRXFactory.createDefaultTalon(talonPortId);
    private PIDController pid;
    // private boolean settingPosition;
    private int currentNumTicks = 0; //revisit this and check if correct for encoder type
    private int desiredNumTicks = 0;
    private double mDesiredOutput;
    private boolean stalled = false;
    private boolean motorOff = false; // off because of current limiting
    private Timer mTimer;
    
    // 1024 ticks/360 degrees = 2.84 ticks/degree (.351 degrees/tick)

    public Arm()
    {
        this.currentNumTicks = 0;
        pid = new PIDController( new PIDGains(kP, kI, kD), SystemSettings.kControlLoopPeriod );
        pid.setInputRange( 0, 383 ); //min and max ticks of arm
        talon.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, SystemSettings.kLongCANTimeoutMs);
        talon.setSelectedSensorPosition(0);

        // init the timer
        this.mTimer = new Timer();
        this.mTimer.reset();
    }

    @Override
    public void modeInit(double pNow)
    {
        
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
        System.out.println("Arm.update: talon sensor ticks = " + this.currentNumTicks);

        // Directly control the output 
        double output = this.mDesiredOutput;

        // Calculate the output to control arm position
        // double output = this.calculateOutput();
        
        // Calculate the current/voltage ration to detect a motor stall
        double current = talon.getOutputCurrent();
        double voltage = talon.getMotorOutputVoltage();
        double ratio = 0.0;

        // When the motor is not running the voltage is zero and divide by zero 
        // is undefined, we will calculate the ratio when the voltage is above
        // a minimum value
        // TODO Parameterize the minimum ratio voltage.
        if ( voltage > 0.5 ) {
            ratio = current / voltage;
        }

        // debug
        System.out.println( "Arm.update initial output = " + output );
        System.out.println( "Arm.update: current = " + current + ", voltage = " + voltage + ", ratio = " + ratio);
        System.out.println( "Arm.update: motorOff = " + this.motorOff + ", stalled = " + this.stalled);


        // If the motor is off check for completion of the cool off period
        if(this.motorOff)
        {
            System.out.println("*************** Motor OFF **********************************************");
            if( mTimer.hasPeriodPassed(MOTOR_OFF_TIME) )
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
            if(ratio > MAX_CURRENT_V_RATIO)
            {
                System.err.println("++++++++++++++++++++++++++ Motor STALLED ++++++++++++++++++++++++++++++++++++++");
                System.out.println( "Arm.update: stalled: " + this.stalled);
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
                    if( mTimer.hasPeriodPassed(MAX_STALL_TIME) )
                    {
                        // We've exceeded the max stall time, stop the motor
                        System.out.println( "Arm.update Max stall time exceeded." );

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

        System.out.println( "Arm.update: About to set talon output = " + output );

        talon.set(ControlMode.PercentOutput, output);
        
    }

    @Override
    public void shutdown(double pNow)
    {

    }

    public int getSetPoints(ESetPoint pPoint)
    {
        switch( pPoint )
        {
            case FULLY_OUT:
            //90 degrees * 2.84 ticks/degree = 256
            return 256;

            case FULLY_UP:
            //135 degrees * 2.84 ticks/degree = 383
            return 383;

            case FULLY_DOWN:
            return 0;

            case MANUAL_STATE:
            default:
            return 0;
        }
    }


    private double calculateOutput() //not running on desired output - check later
    {
        pid.setSetpoint(this.desiredNumTicks);
        double tempOutput = pid.calculate( talon.getSelectedSensorPosition(), Timer.getFPGATimestamp() );

        // Constrain output to min/max values for the Talon
        return Util.limit(tempOutput, -1, 1);

    }

    private int angleToTicks(double angle)
    {
        // 1024 ticks/360 degrees = 2.84 ticks/degree (.351 degrees/tick)
        // TODO Parametrize this constant, also calculate the constant for more accuracy
        return (int) (angle * 2.84);
    }

    private double ticksToAngle(int numTicks)
    {
        // 1024 ticks/360 degrees = 2.84 ticks/degree (.351 degrees/tick)
        // TODO Parametrize this constant, also calculate the constant for more accuracy
        return numTicks * .351;
    }

    /**
     * Choose a predetermined set point.
     */
    public void setPoint(ESetPoint pDesiredPoint) 
    {
        this.desiredNumTicks = getSetPoints(pDesiredPoint);
    }

    public void setArmAngle( double angle )
    {
        // TODO Parameterize the angle limits
        // Constrain the angle to the allowed values
        angle = Util.limit(angle, 0, 135);
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