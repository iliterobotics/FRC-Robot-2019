package us.ilite.robot.modules;

public class Arm extends Module
{
    private double mVoltage;
    private double mTheta;

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
        double p_gain = 0.4; //adjust
        double d_gain = 6.5; //adjust
        double i_gain = 0.001; //adjust
      
        double error = pGoal - pTheta;
        //double deriv = pTheta - this.getGlobal("last_x");
        //double integral = this.getGlobal("integral") + error;
      
        //this.setGlobal("last_x", pTheta);
        //this.setGlobal("integral", integral);
      
        //return (error * p_gain) + (integral * i_gain) - (deriv * d_gain);
        return 0.0;
      }


      public void getSetPoints( SetPoint pPoint)
      {
        switch( pPoint )
        {
            case FULLY_OUT:
            break;

            case FULLY_UP:
            break;

            case FULLY_DOWN:
            break;
        }
      }

}