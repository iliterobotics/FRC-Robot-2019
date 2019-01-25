package us.ilite.common.lib.util;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

public class NetworkTableInstanceWrapper implements ITableProvider {

    private static NetworkTableInstance sInstance = NetworkTableInstance.getDefault();

    @Override
    public ITable getTable(String tableName) {
        return new NetworkTableWrapper(sInstance.getTable(tableName));
    }

    private static class NetworkTableWrapper implements ITable {
        private final NetworkTable networkTable;
        public NetworkTableWrapper(NetworkTable networkTable) {
            this.networkTable = networkTable;
        }
        @Override
        public ITableEntry getEntry(String entryName) {
            NetworkTableEntry tableEntry = networkTable.getEntry(entryName);
            return new NetworkEntryWrapper(tableEntry);

        }
    }

    private static class NetworkEntryWrapper implements ITableEntry {
        private final NetworkTableEntry entry;

        public NetworkEntryWrapper(NetworkTableEntry entry) {
            this.entry = entry;
        }

        @Override
        public boolean exists() {
            return entry.exists();
        }
        @Override
        public String getString(String key) {
            return entry.getString(key);
        }
        @Override
        public boolean setString(String newString) {
           return entry.setString(newString);
        }

    }
    


}