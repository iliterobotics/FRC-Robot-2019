package us.ilite.lib.drivers;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import com.revrobotics.CANSparkMax;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SparkMaxUtil {

    private static final ILog sLog = Logger.createLog(SparkMaxUtil.class);

    public static void reportFaults(CANSparkMax pCANSparkMax) {
        if(hasFaults(pCANSparkMax)) {
            getFaultList(pCANSparkMax, fault -> sLog.error("Fault ", fault.name(), " detected on Spark MAX ID ", pCANSparkMax.getDeviceId()));
        }
    }

    public static void reportStickyFaults(CANSparkMax pCANSparkMax) {
        if(hasFaults(pCANSparkMax)) {
            getStickyFaultList(pCANSparkMax, fault -> sLog.error("Sticky Fault ", fault.name(), " detected on Spark MAX ID ", pCANSparkMax.getDeviceId()));
        }
    }

    /**
     * The Spark MAX doesn't provide a method to get all the faults from the controller, but
     * it does provide a way to check for the presence of a specific fault. This utility
     * lets us loop through and collect a list of all faults at once.
     * @param pCANSparkMax The Spark MAX to check for faults.
     * @return A list of faults present on the Spark MAX.
     */
    public static List<CANSparkMax.FaultID> getFaultList(CANSparkMax pCANSparkMax, Consumer<CANSparkMax.FaultID> pErrorConsumer) {
        List<CANSparkMax.FaultID> faults = new ArrayList<>();

        for(CANSparkMax.FaultID fault : CANSparkMax.FaultID.values()) {
            if(pCANSparkMax.getFault(fault)) {
                faults.add(fault);
                pErrorConsumer.accept(fault);
            }
        }

        return faults;
    }

    /**
     * The Spark MAX doesn't provide a method to get all the faults from the controller, but
     * it does provide a way to check for the presence of a specific fault. This utility
     * lets us loop through and collect a list of all faults at once.
     * @param pCANSparkMax The Spark MAX to check for faults.
     * @return A list of faults present on the Spark MAX.
     */
    public static List<CANSparkMax.FaultID> getStickyFaultList(CANSparkMax pCANSparkMax, Consumer<CANSparkMax.FaultID> pErrorConsumer) {
        List<CANSparkMax.FaultID> faults = new ArrayList<>();

        for(CANSparkMax.FaultID fault : CANSparkMax.FaultID.values()) {
            if(pCANSparkMax.getStickyFault(fault)) {
                faults.add(fault);
                pErrorConsumer.accept(fault);
            }
        }

        return faults;
    }

    public static List<CANSparkMax.FaultID> getFaultList(CANSparkMax pCANSparkMax) {
        return getFaultList(pCANSparkMax, pS -> {});
    }

    public static List<CANSparkMax.FaultID> getStickyFaultList(CANSparkMax pCANSparkMax) {
        return getStickyFaultList(pCANSparkMax, pS -> {});
    }

    /**
     * Returns whether the Spark MAX has any faults.
     * @return Whether the Spark MAX currently has any faults present.
     */
    public static boolean hasFaults(CANSparkMax pCANSparkMax) {
        // Faults are stored in a 16-bit variable. Each bit represents a fault when its value is 1.
        return pCANSparkMax.getFaults() != 0;
    }

    public static boolean hasStickyFaults(CANSparkMax pCANSparkMax) {
        return pCANSparkMax.getStickyFaults() != 0;
    }

}
