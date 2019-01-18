package us.ilite.robot.modules;
import us.ilite.common.config.SystemSettings;
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.Ultrasonic;

public class UltrasonicSensor 
{
    private AnalogInput mUltrasonicSensor;
    private double mDistanceInch;

    public UltrasonicSensor()
    {
        mUltrasonicSensor = new AnalogInput(SystemSettings.ULTRASONIC_PORT );
    }

    public double getDistanceInches()
    {
        double volts = mUltrasonicSensor.getVoltage();
        mDistanceInch = volts * 9.83 * 0.0393701;
        return mDistanceInch;
    }

    public double getVoltage(){
        return mUltrasonicSensor.getVoltage();
    }
}