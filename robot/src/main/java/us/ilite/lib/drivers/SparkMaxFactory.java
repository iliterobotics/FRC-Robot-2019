/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package us.ilite.lib.drivers;

/**
 * This is a factory class for the Spark MAX motor controller that
 * re-configures all settings to our defaults. Note that settings must be
 * explicitly flashed to the Spark MAX in order to persisten across power cycles.
 * 
 */
public class SparkMaxFactory {

    public static class Configuration {
        
    }

    private static final Configuration kDefaultConfiguration = new Configuration();
    private static final Configuration kSlaveConfiguration = new Configuration();

    public CANSparkMax createDefaultSparkMax(int pId, MotorType pMotorType) {
        return createSparkMax(pId, pMotorType, kDefaultConfiguration);
    }

    public CANSparkMax createPermanentSlaveSparkMax(int pId, MotorType pMotorType) {

    }

    public CANSparkMax createSparkMax(int pId, MotorType pMotorType, Configuration pConfiguration) {

    }

}
