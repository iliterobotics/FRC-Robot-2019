package us.ilite.robot.modules;
import us.ilite.common.config.SystemSettings;
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.Ultrasonic;

public class RockwellUltrasonic 
{

    private static final double kMinMeasuringDistance = 1.75;
    private static final double kInchesPerVolt = 3.4;

    private AnalogInput mUltrasonicSensor;

    public RockwellUltrasonic()
    {
        mUltrasonicSensor = new AnalogInput(SystemSettings.ULTRASONIC_PORT);
    }

    /**
     * Distance is directly proportional to voltage using the equation:
     * distance = inches_per_volt * volts + min_distance
     * We obtained inches_per_volt by finding the minimum measuring distance
     * and the voltage from the sensor at a known distance, then found 
     * inches_per_volt = (known_distance - min_distance) / voltage.
     * 
     * @return The distance sensed by the ultrasonic sensor
     */
    public double getDistanceInches() {
        return (getVoltage() * kInchesPerVolt) + kMinMeasuringDistance;
    }

    public double getVoltage() {
        return mUltrasonicSensor.getVoltage();
    }

}