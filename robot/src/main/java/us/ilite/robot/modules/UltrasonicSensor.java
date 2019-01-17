import org.ilite.frc.commor.config.SystemSettings;
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.Ultrasonic.

public class UltrasonicSensor implements IModule 
{
    private AnalogInput mUltrasonicSensor;
    private double mDistanceInch;

    public UltrasonicSensor()
    {
        mUltrasonicSensor = new AnalogInput( SystemSettings.ULTRASONIC_PORT );
    }

    public double getDistanceInches()
    {
        double volts = AnalogInput.getVoltage();
        mDistanceInch = volts * 45 * 0.0393701;
        return mDistanceInch;
    }

}