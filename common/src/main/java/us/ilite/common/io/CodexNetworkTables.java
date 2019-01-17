package us.ilite.common.io;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.flybotix.hfr.codex.Codex;
import com.flybotix.hfr.codex.CodexOf;
import com.flybotix.hfr.util.lang.EnumUtils;
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
   * @param pEnum
   */
  public <V, E extends Enum<E> & CodexOf<V>> void registerCodex(Class<E> pEnum) {
    Integer hash = EnumUtils.hashOf(pEnum);
    String tablename = pEnum.getSimpleName().toUpperCase();
    mLog.debug("Registering codex " + tablename + " with hash " + hash);
    mTables.put(hash, sNETWORK_TABLES.getTable(tablename));

    mWriters.put(hash, ((nt, key, val) -> nt.getEntry(key).setValue(val)));
  }

  /**
   * Writes the elements and metadata values of the codex to the NetworkTables that
   * corresponds to the Codex's enum class name.s
   * @param pCodex
   */
  public <V, E extends Enum<E> & CodexOf<V>> void send(Codex<V,E> pCodex) {
    int hash = EnumUtils.hashOf(pCodex.meta().getEnum());

    if(!mWriters.containsKey(hash) || !mTables.containsKey(hash)) {
      mLog.warn("Cannot send codex " + pCodex.meta().getEnum().getSimpleName() + " because it has not been registered.");
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

      // grumble grumble.... NT doesn't have a way to clear a field.
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
