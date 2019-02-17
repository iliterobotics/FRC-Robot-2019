package us.ilite.lib.drivers;

import com.revrobotics.CANEncoder;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMax.ExternalFollower;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.ConfigParameter;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.revrobotics.CANSparkMaxLowLevel.PeriodicFrame;

import us.ilite.common.config.SystemSettings;

/**
 * This is a factory class for the Spark MAX motor controller that re-configures
 * all settings to our defaults. Note that settings must be explicitly flashed
 * to the Spark MAX in order to persisten across power cycles.
 * 
 */
public class SparkMaxFactory {

    public static class Configuration {
        public int CAN_TIMEOUT = 100;
        public int CONTROL_FRAME_PERIOD = 10;
        public IdleMode IDLE_MODE = IdleMode.kBrake;
        public boolean IS_INVERTED = false;
        public int STATUS_0_PERIOD_MS = 10;
        public int STATUS_1_PERIOD_MS = 20;
        public int STATUS_2_PERIOD_MS = 50;
        public double RAMP_RATE = 0.0;
        public int SMART_CURRENT_LIMIT = 80;
        public double SECONDARY_CURRENT_LIMIT = 0.0;
    }

    private static final Configuration kDefaultConfiguration = new Configuration();
    private static final Configuration kSlaveConfiguration = new Configuration();

    static {
        // kSlaveConfiguration.CONTROL_FRAME_PERIOD = 100;
    }

    public static CANSparkMax createDefaultSparkMax(int pId, MotorType pMotorType) {
        return createSparkMax(pId, pMotorType, kDefaultConfiguration);
    }

    public static CANSparkMax createPermanentSlaveSparkMax(int pId, int pFollowerId, MotorType pMotorType, ExternalFollower pExternalFollower) {
        CANSparkMax spark = createSparkMax(pId, pMotorType, kSlaveConfiguration);
        spark.follow(ExternalFollower.kFollowerSparkMax, pFollowerId);
        return spark;
    }

    public static CANSparkMax createSparkMax(int pId, MotorType pMotorType, Configuration pConfiguration) {
        CANSparkMax spark = new CANSparkMax(pId, pMotorType);

        spark.setCANTimeout(pConfiguration.CAN_TIMEOUT);
//        spark.setControlFramePeriod(pConfiguration.CONTROL_FRAME_PERIOD);
        spark.setIdleMode(pConfiguration.IDLE_MODE);
        spark.setInverted(pConfiguration.IS_INVERTED);
        spark.setPeriodicFramePeriod(PeriodicFrame.kStatus0, pConfiguration.STATUS_0_PERIOD_MS);
        spark.setPeriodicFramePeriod(PeriodicFrame.kStatus1, pConfiguration.STATUS_1_PERIOD_MS);
        spark.setPeriodicFramePeriod(PeriodicFrame.kStatus2, pConfiguration.STATUS_2_PERIOD_MS);
//        spark.setRampRate(pConfiguration.RAMP_RATE);
        spark.setSecondaryCurrentLimit(pConfiguration.SECONDARY_CURRENT_LIMIT);
        spark.setSmartCurrentLimit(pConfiguration.SMART_CURRENT_LIMIT);

        return spark;
    }

}
