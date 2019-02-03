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
    private double kP;
    private double kI;
    private double kD;
    private int maxNumTicks = 383; //number of ticks at max arm angle
    private int minNumTicks = 0; //number of ticks at min arm angle
    private static int talonPortId = 6; //placeholder constant; change later in SystemSettings
    private TalonSRX talon = TalonSRXFactory.createDefaultTalon(talonPortId);
    private PIDController pid;
    // private boolean settingPosition;
    private int currentNumTicks = 0; //revisit this and check if correct for encoder type
    private int desiredNumTicks = 0;
    private double mDesiredOutput;
    private boolean stalled = false;
    private boolean motorOff = false; //off because of current limiting
    private Timer mTimer = new Timer();
    
    // 1024 ticks/360 degrees = 2.84 ticks/degree (.351 degrees/tick)

    public Arm()
    {
        this.currentNumTicks = 0;
        pid = new PIDController( new PIDGains(kP, kI, kD), SystemSettings.kControlLoopPeriod );
        pid.setInputRange( 0, 383 ); //min and max ticks of arm
        talon.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, SystemSettings.kLongCANTimeoutMs);
        talon.setSelectedSensorPosition(0);
    }

    @Override
    public void modeInit(double pNow)
    {
        
    }

    @Override
    public void periodicInput(double pNow)
    {
        
    }

    @Override
    public void update(double pNow)
    {
        currentNumTicks = talon.getSelectedSensorPosition();
        System.out.println( "desired output a = " + this.mDesiredOutput );
        double current = talon.getOutputCurrent();
        double voltage = talon.getMotorOutputVoltage();
        double ratio = current / voltage;

        // if(ratio > MAX_CURRENT_V_RATIO)
        // {
        //     System.out.println( "desired output b = " + this.mDesiredOutput );
        //     if(!this.stalled)
	    //     {
        //         this.stalled = true;
        //         mTimer.start();
        //     }
        //     else
        //     {
        //         if( mTimer.hasPeriodPassed(MAX_STALL_TIME) )
        //         {
        //             System.out.println( "desired output c = " + this.mDesiredOutput );
        //             this.mDesiredOutput = 0;
        //             this.motorOff = true;
        //             mTimer.stop();
        //             mTimer.start(); //starting for cool-off period
        //         }
        //     }
        // }
        // else
        // {
        //     this.stalled = false;
        // }

        // if(this.motorOff)
        // {
        //     if( mTimer.hasPeriodPassed(MOTOR_OFF_TIME) )
        //     {
        //         this.motorOff = false;
        //         this.mTimer.stop();
        //     }
        //     else
        //     {
        //         System.out.println( "desired output d = " + this.mDesiredOutput );
        //         this.mDesiredOutput = 0;
        //     }
        // }
        System.out.println( "about to set desired output = " + this.mDesiredOutput );
        System.out.println("ticks =" + this.currentNumTicks);
        this.currentNumTicks = talon.getSelectedSensorPosition();
        talon.set(ControlMode.PercentOutput, this.mDesiredOutput);
        
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

        }
        return 0;
    }


    private double calculateOutput() //not running on desired output - check later
    {
        pid.setSetpoint(this.desiredNumTicks);
        double tempOutput = pid.calculate( talon.getSelectedSensorPosition(), Timer.getFPGATimestamp() );
        return Util.limit(tempOutput, -1, 1);

    }

    private int angleToTicks(double angle)
    {
        // 1024 ticks/360 degrees = 2.84 ticks/degree (.351 degrees/tick)
        return (int) (angle * 2.84);
    }

    private double ticksToAngle(int numTicks)
    {
        // 1024 ticks/360 degrees = 2.84 ticks/degree (.351 degrees/tick)
        return numTicks * .351;
    }

    public void setArmAngle( double angle )
    {
        angle = Util.limit(angle, 0, 135);
        this.desiredNumTicks = angleToTicks( angle );
    }

    public void setPoint(ESetPoint pDesiredPoint) 
    {
        this.desiredNumTicks = getSetPoints(pDesiredPoint);
    }

    public void setDesiredOutput( double desiredOutput )
    {
        this.mDesiredOutput = Util.limit(desiredOutput, -1, 1);
    }

    public double getCurrentArmAngle()
    {
        return ticksToAngle( this.currentNumTicks );
    }

}