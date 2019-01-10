package us.ilite.lib.util;

import java.util.List;

import com.flybotix.hfr.codex.Codex;
import com.flybotix.hfr.codex.CodexOf;
import com.flybotix.hfr.util.lang.EnumUtils;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import us.ilite.common.config.SystemSettings;
import us.ilite.robot.Data;

public class SimpleNetworkTable  {
    private NetworkTable netTable;
    
    public SimpleNetworkTable(String name) {
        NetworkTableInstance.getDefault().setUpdateRate(SystemSettings.NETWORK_TABLE_UPDATE_RATE);
        netTable = NetworkTableInstance.getDefault().getTable(name);

        netTable.getInstance().setUpdateRate(SystemSettings.NETWORK_TABLE_UPDATE_RATE);
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
    
    public synchronized void putNumberArray(String key, Integer[] values) {
    		netTable.getEntry(key).setNumberArray(values);
    }
    
    public synchronized void putString(String key, String value) {
    		netTable.getEntry(key).forceSetString(value);
    }
    
    public synchronized NetworkTable getInstance() {
    		return netTable;
    }



    /**
     * Provides a way to write every value of a codex to NetworkTables.
     * @param pCodex The codex you want to dump to NetworkTables.
     */
    public static <V extends Number, E extends Enum<E> & CodexOf<V>> void writeCodexToSmartDashboard(Class<E> pEnumeration, Codex<V, E> pCodex, double pTime) {
        writeCodexToSmartDashboard(pEnumeration.getSimpleName(), pCodex, pTime);
    }

    /**
     *
     * @param name Allows you to define a name for the codex so two of the same type can be written at once.
     * @param pCodex The codex you want to dump to NetworkTables.
     * @param pTime The current time.
     */
    public static <V extends Number, E extends Enum<E> & CodexOf<V>> void writeCodexToSmartDashboard(String name, Codex<V, E> pCodex, double pTime) {
        List<E> enums = EnumUtils.getSortedEnums(pCodex.meta().getEnum());
        for(E e : enums) {
            Double value = (Double) pCodex.get(e);
            if(e != null) logNumber(name, e, value);
        }
        logNumber(name, SystemSettings.kLoggingTimestampKey, pTime);
    }

    public static <E extends Enum<E>> void logNumber(String pName, E pEnumeration, Number pNumber) {
        logNumber(pName, pEnumeration.toString(), pNumber);
    }

    public static <E extends Enum<E>> void logNumber(String pName, String key, Number pNumber) {
        Data.kLoggingTable.putDouble(pName + "-" + key, (pNumber == null) ? 0 : pNumber.doubleValue());
    }

}