package us.ilite.common.lib.util;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import us.ilite.common.config.SystemSettings;


public class SimpleNetworkTable  {
    private NetworkTable netTable;
            
    public SimpleNetworkTable(String name) {
        NetworkTableInstance.getDefault().setUpdateRate(SystemSettings.kNetworkTableUpdateRate);
        netTable = NetworkTableInstance.getDefault().getTable(name);

        netTable.getInstance().setUpdateRate(SystemSettings.kNetworkTableUpdateRate);
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

}