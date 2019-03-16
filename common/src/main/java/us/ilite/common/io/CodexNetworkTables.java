package us.ilite.common.io;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.flybotix.hfr.codex.Codex;
import com.flybotix.hfr.codex.CodexOf;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

public class CodexNetworkTables {
  private final ILog mLog = Logger.createLog(CodexNetworkTables.class);
  private Map<Integer, NetworkTable> mTables = new HashMap<>();
  private Map<Integer, Map<String, NetworkTableEntry>> mEntries = new HashMap<>();
  private Map<Integer, Put<?>> mWriters = new HashMap<>();
  private final static NetworkTableInstance sNETWORK_TABLES = NetworkTableInstance.getDefault();

  /**
   * Initializes a few items related to writing elements of a codex to a network table.
   * Do this ahead of time to prevent issues with timing on the first cycle.
   */
  public <V, E extends Enum<E> & CodexOf<V>> void registerCodex(Codex<V, E> pCodex) {
    Class<E> enumClass = pCodex.meta().getEnum();
    registerCodexWithTableName(CodexNetworkTablesParser.constructNetworkTableName(enumClass), pCodex);
  }

  /**
   * Initializes a few items related to writing elements of a codex to a network table.
   * Do this ahead of time to prevent issues with timing on the first cycle.
   */
  public <V, E extends Enum<E> & CodexOf<V>> void registerCodex(String pName, Codex<V, E> pCodex) {
    Class<E> enumClass = pCodex.meta().getEnum();
    registerCodexWithTableName(CodexNetworkTablesParser.constructNetworkTableName(enumClass, pName), pCodex);
  }
  
  private <V, E extends Enum<E> & CodexOf<V>> void registerCodexWithTableName(String pTableName, Codex<V, E> pCodex) {
      Integer hash = pTableName.hashCode();
      mLog.debug("Registering codex " + pTableName + " with hash " + hash);
      mTables.put(hash, sNETWORK_TABLES.getTable(pTableName));
  
      mWriters.put(hash, ((nt, key, val) -> nt.getEntry(key).setValue(val)));
  }

  /**
   * Writes the elements and metadata values of the codex to the NetworkTables that
   * corresponds to the Codex's enum class name.
   */
  public <V, E extends Enum<E> & CodexOf<V>> void send(Codex<V,E> pCodex) {
    String tableName = CodexNetworkTablesParser.constructNetworkTableName(pCodex.meta().getEnum());
    sendWithTableName(tableName, pCodex);
  }

  /**
   * Writes the elements and metadata values of the codex to the NetworkTables that
   * corresponds to the Codex's enum class name.
   */
  public <V, E extends Enum<E> & CodexOf<V>> void send(String pName, Codex<V,E> pCodex) {
    String tableName = CodexNetworkTablesParser.constructNetworkTableName(pCodex.meta().getEnum(), pName);
    sendWithTableName(tableName, pCodex);
  }

  private <V, E extends Enum<E> & CodexOf<V>> void sendWithTableName(String pTableName, Codex<V,E> pCodex) {
    int hash = pTableName.hashCode();

    if(!mWriters.containsKey(hash) || !mTables.containsKey(hash)) {
      mLog.warn("Cannot send codex " + pTableName + " because it has not been registered.");
      return;
    }

    @SuppressWarnings("unchecked")
    Put<V> writer = (Put<V>)mWriters.get(hash);
    NetworkTable nt = mTables.get(hash);

    nt.getEntry("ID").setNumber(pCodex.meta().id());
    nt.getEntry("KEY").setNumber(pCodex.meta().key());
    nt.getEntry("TIME_MS").setNumber(pCodex.meta().timestamp());
    for(E e : EnumSet.allOf(pCodex.meta().getEnum())) {
      if(pCodex.isSet(e)) {
        writer.write(nt, e.name().toUpperCase(), pCodex.get(e));
      }
    }
  }

  private CodexNetworkTables() {
  }

  public static CodexNetworkTables getInstance() {
    return Holder.instance;
  }

  private static class Holder {
    private final static CodexNetworkTables instance = new CodexNetworkTables();
  }

 private interface Put<V> {
   void write(NetworkTable nt, String pKey, V pValue);
 }

}
