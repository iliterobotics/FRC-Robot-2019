package com.team254.lib.drivers.talon;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleSupplier;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.team254.lib.util.Util;

import edu.wpi.first.wpilibj.Timer;
import us.ilite.robot.modules.Module;

public class TalonSRXChecker {
    public static class CheckerConfigBuilder { 
        private double mCurrentFloor = 5;
        private double mRPMFloor = 2000;

        private double mCurrentEpsilon = 5.0;
        private double mRPMEpsilon = 500;
        private DoubleSupplier mRPMSupplier = null;

        private double mRunTimeSec = 4.0;
        private double mWaitTimeSec = 2.0;
        private double mRunOutputPercentage = 0.5;
		/**
		 * @return the currentFloor
		 */
		public double getCurrentFloor() {
			return mCurrentFloor;
		}
		/**
		 * @param currentFloor the currentFloor to set
		 */
		public void setCurrentFloor(double currentFloor) {
			this.mCurrentFloor = currentFloor;
		}
		/**
		 * @return the RPMFloor
		 */
		public double getRPMFloor() {
			return mRPMFloor;
		}
		/**
		 * @param RPMFloor the RPMFloor to set
		 */
		public void setRPMFloor(double RPMFloor) {
			this.mRPMFloor = RPMFloor;
		}
		/**
		 * @return the currentEpsilon
		 */
		public double getCurrentEpsilon() {
			return mCurrentEpsilon;
		}
		/**
		 * @param currentEpsilon the currentEpsilon to set
		 */
		public void setCurrentEpsilon(double currentEpsilon) {
			this.mCurrentEpsilon = currentEpsilon;
		}
		/**
		 * @return the RPMEpsilon
		 */
		public double getRPMEpsilon() {
			return mRPMEpsilon;
		}
		/**
		 * @param RPMEpsilon the RPMEpsilon to set
		 */
		public void setRPMEpsilon(double RPMEpsilon) {
			this.mRPMEpsilon = RPMEpsilon;
		}
		/**
		 * @return the RPMSupplier
		 */
		public DoubleSupplier getRPMSupplier() {
			return mRPMSupplier;
		}
		/**
		 * @param RPMSupplier the RPMSupplier to set
		 */
		public void setRPMSupplier(DoubleSupplier RPMSupplier) {
			this.mRPMSupplier = RPMSupplier;
		}
		/**
		 * @return the runTimeSec
		 */
		public double getRunTimeSec() {
			return mRunTimeSec;
		}
		/**
		 * @param RunTimeSec the runTimeSec to set
		 */
		public void setmRunTimeSec(double runTimeSec) {
			this.mRunTimeSec = runTimeSec;
		}
		/**
		 * @return the waitTimeSec
		 */
		public double getWaitTimeSec() {
			return mWaitTimeSec;
		}
		/**
		 * @param waitTimeSec the waitTimeSec to set
		 */
		public void setmWaitTimeSec(double waitTimeSec) {
			this.mWaitTimeSec = waitTimeSec;
		}
		/**
		 * @return the runOutputPercentage
		 */
		public double getRunOutputPercentage() {
			return mRunOutputPercentage;
		}
		/**
		 * @param mRunOutputPercentage the mRunOutputPercentage to set
		 */
		public void setRunOutputPercentage(double runOutputPercentage) {
			this.mRunOutputPercentage = runOutputPercentage;
        }
        
        public CheckerConfig build() { 
            return new CheckerConfig(this);
        }


    }
    public static class CheckerConfig {
        private final double mCurrentFloor;
        private final double mRPMFloor;

        private final double mCurrentEpsilon;
        private final double mRPMEpsilon;
        private final DoubleSupplier mRPMSupplier;

        private final double mRunTimeSec;
        private double mWaitTimeSec;
        private double mRunOutputPercentage;

        private CheckerConfig(CheckerConfigBuilder builder) {
            mCurrentFloor = builder.getCurrentFloor();
            mRPMFloor = builder.getRPMFloor();
            mCurrentEpsilon = builder.getCurrentEpsilon();
            mRPMEpsilon = builder.getRPMEpsilon();
            mRPMSupplier = builder.getRPMSupplier();
            mRunTimeSec = builder.getRunTimeSec();
            mWaitTimeSec = builder.getWaitTimeSec();
            mRunOutputPercentage = builder.getRunOutputPercentage();
        }
    }

    public static class TalonSRXConfig {
        public String mName;
        public TalonSRX mTalon;

        public TalonSRXConfig(String name, TalonSRX talon) {
            mName = name;
            mTalon = talon;
        }
    }

    private static class StoredTalonSRXConfiguration {
        public ControlMode mMode;
        public double mSetValue;
    }

    public static <E extends Module> boolean CheckTalons(Class<E> subsystem,
                                      List<TalonSRXConfig> talonsToCheck,
                                      CheckerConfig checkerConfig) {
        boolean failure = false;
        System.out.println("////////////////////////////////////////////////");
        System.out.println("Checking subsystem " + subsystem
                + " for " + talonsToCheck.size() + " talons.");

        List<Double> currents = new ArrayList<>();
        List<Double> rpms = new ArrayList<>();
        List<StoredTalonSRXConfiguration> storedConfigurations = new ArrayList<>();

        // Record previous configuration for all talons.
        for (TalonSRXConfig config : talonsToCheck) {
            LazyTalonSRX talon = LazyTalonSRX.class.cast(config.mTalon);

            StoredTalonSRXConfiguration configuration = new StoredTalonSRXConfiguration();
            configuration.mMode = talon.getControlMode();
            configuration.mSetValue = talon.getLastSet();

            storedConfigurations.add(configuration);

            // Now set to disabled.
            talon.set(ControlMode.PercentOutput, 0.0);
        }

        for (TalonSRXConfig config : talonsToCheck) {
            System.out.println("Checking: " + config.mName);

            config.mTalon.set(ControlMode.PercentOutput, checkerConfig.mRunOutputPercentage);
            Timer.delay(checkerConfig.mRunTimeSec);

            // Now poll the interesting information.
            double current = config.mTalon.getOutputCurrent();
            currents.add(current);
            System.out.print("Current: " + current);

            double rpm = Double.NaN;
            if (checkerConfig.mRPMSupplier != null) {
                rpm = checkerConfig.mRPMSupplier.getAsDouble();
                rpms.add(rpm);
                System.out.print(" RPM: " + rpm);
            }
            System.out.print('\n');

            config.mTalon.set(ControlMode.PercentOutput, 0.0);

            // And perform checks.
            if (current < checkerConfig.mCurrentFloor) {
                System.out.println(config.mName + " has failed current floor check vs " +
                        checkerConfig.mCurrentFloor + "!!");
                failure = true;
            }
            if (checkerConfig.mRPMSupplier != null) {
                if (rpm < checkerConfig.mRPMFloor) {
                    System.out.println(config.mName + " has failed rpm floor check vs " +
                            checkerConfig.mRPMFloor + "!!");
                    failure = true;
                }
            }

            Timer.delay(checkerConfig.mWaitTimeSec);
        }

        // Now run aggregate checks.

        if (currents.size() > 0) {
            Double average = currents.stream().mapToDouble(val -> val).average().getAsDouble();

            if (!Util.allCloseTo(currents, average, checkerConfig.mCurrentEpsilon)) {
                System.out.println("Currents varied!!!!!!!!!!!");
                failure = true;
            }
        }

        if (rpms.size() > 0) {
            Double average = rpms.stream().mapToDouble(val -> val).average().getAsDouble();

            if (!Util.allCloseTo(rpms, average, checkerConfig.mRPMEpsilon)) {
                System.out.println("RPMs varied!!!!!!!!");
                failure = true;
            }
        }

        // Restore Talon configurations
        for (int i = 0; i < talonsToCheck.size(); ++i) {
            talonsToCheck.get(i).mTalon.set(storedConfigurations.get(i).mMode,
                    storedConfigurations.get(i).mSetValue);
        }

        return !failure;
    }
}
