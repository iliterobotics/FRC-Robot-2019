package us.ilite.robot.modules;

import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import us.ilite.common.config.SystemSettings;
import us.ilite.common.lib.control.PIDController;

public class Arm extends Module
{
    private double mVoltage;
    private double mTheta;
    private double mDesiredTheta;
    private ESetPoint mDesiredSetPoint;
    private ESetPoint mCurrentSetPoint;
    private static int talonPortId = 0; //placeholder constant; change later in SystemSettings

    // 1024 ticks/360 degrees = .351 t/d

    public Arm()
    {
        TalonSRX talon = new TalonSRX(talonPortId);
    }

    @Override
    public void modeInit(double pNow)
    {
        //PIDController pid = PIDController( new PIDController(kP, kI, kD), SystemSettings.kControlLoopPeriod );
        // pid.setInputRange( min, max );
        //
    }

    @Override
    public void periodicInput(double pNow)
    {

    }

    @Override
    public void update(double pNow)
    {
        //mTheta = updated angle;
        //motor output = mVoltage * cos( mTheta );
    }

    @Override
    public void shutdown(double pNow)
    {

    }

    public void moveByAngle( double pGoalTheta )
    {
        //motor output += returnPID( mTheta, pGoalTheta );
    }

    public double returnPID(double pTheta, double pGoal)
    {
        double p_gain = 0.0; //adjust
        double d_gain = 0.0; //adjust
        double i_gain = 0.0; //adjust
      
        double error = pGoal - pTheta;

     
        return 0.0;
      }


      public void getSetPoints( ESetPoint pPoint)
      {
        switch( pPoint )
        {
            case FULLY_OUT:
            //while ( !button to change position is pressed ){}
            break;

            case FULLY_UP:
            //while ( !button to change position is pressed ){}
            break;

            case FULLY_DOWN:
            //while ( !button to change position is pressed ){}
            break;

            case MANUAL_STATE:
            //while ( !button to change position is pressed ){}
            break;
        }
      }

}