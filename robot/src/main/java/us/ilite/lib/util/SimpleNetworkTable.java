package us.ilite.lib.util;

import edu.wpi.first.networktables.*;
import us.ilite.common.config.SystemSettings;

public class SimpleNetworkTable {
    private NetworkTable netTable;

    public SimpleNetworkTable(String name) {
        NetworkTableInstance.getDefault().setUpdateRate(SystemSettings.NETWORK_TABLE_UPDATE_RATE);
        netTable = NetworkTableInstance.getDefault().getTable(name);

        netTable.getInstance().setServerTeam(1885);
        netTable.getInstance().startClientTeam(1885);
    }

    public void initKeys() {

    }

    public synchronized NetworkTableEntry getEntry(String key) {
        return netTable.getEntry(key);
    }

    public synchronized void putDouble(String key, double value) {
        netTable.getEntry(key).forceSetDouble(value);
    }

    public synchronized void putNumber(String key, Integer value) {
        netTable.getEntry(key).forceSetNumber(value);
    }

    public synchronized void putNUmberArray(String key, Integer[] value) {
        netTable.getEntry(key).forceSetNumberArray(value);
    }

    // /**
    //  *
    //  * @param name Allows you to define a name for the codex so two of the same type can be written at once.
    //  * @param pCodex The codex you want to dump to NetworkTables.
    //  * @param pTime The current time.
    //  */
    // public static <V extends Number, E extends Enum<E> & CodexOf<V>> void writeCodexToSmartDashboard(String name, Codex<V, E> pCodex, double pTime) {
    //     Class<E> enumClass = pCodex.meta().getEnum();
    //     E [] enumConstants = enumClass.getEnumConstants();
    //     for(E e : enumConstants) {
    //         Double value = (Double) pCodex.get(e);
    //         if(e != null) logNumber(name, e, value);
    //     }
    //     logNumber(name, SystemSettings.kLoggingTimestampKey, pTime);
    // }
    public synchronized void putString(String key, String value) {
        netTable.getEntry(key).forceSetString(value);
    }

    public synchronized NetworkTable getInstance() {
        return netTable;        
    }

}