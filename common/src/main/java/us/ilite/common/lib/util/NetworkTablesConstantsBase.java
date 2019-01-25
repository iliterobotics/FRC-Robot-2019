package us.ilite.common.lib.util;

import java.lang.reflect.Field;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

/**
 * Add your docs here.
 */
public abstract class NetworkTablesConstantsBase {

    private final ILog mLog = Logger.createLog(NetworkTablesConstantsBase.class);
    private final ITable mTable;
    private final Field[] mDeclaredFields;
    private final Gson mGson;

    public NetworkTablesConstantsBase(ITableProvider tableProvider) {
        mTable = tableProvider.getTable(this.getClass().getSimpleName().toUpperCase());
        mDeclaredFields = this.getClass().getDeclaredFields();
        mGson = new Gson();
    }

    public void writeToNetworkTables() {
        for (Field f : mDeclaredFields) {
            if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
                ITableEntry entry = mTable.getEntry(f.getName());

                try {
                    entry.setString(mGson.toJson(f.get(this)));
                } catch (IllegalAccessException e) {
                    mLog.error("Could not write value of ", f.getName(), " to NetworkTables. Maybe the variable is final?");
                    mLog.exception(e);
                } catch (Exception e) {
                    mLog.error("Could not write value of ", f.getName(), " to NetworkTables.");
                    mLog.exception(e);
                }
            }
        }
    }

    public void loadFromNetworkTables() {
        for (Field f : mDeclaredFields) {
            ITableEntry entry = mTable.getEntry(f.getName());
            if(entry.exists()) {
                try {
                    f.set(this, mGson.fromJson(entry.getString(""), f.getGenericType()));
                } catch (IllegalAccessException e) {
                    mLog.error("Could not load value of ", f.getName(), " from NetworkTables. Maybe the variable is final?");
                    mLog.exception(e);
                } catch(JsonParseException e) {
                    mLog.error("Failed parsing value of ", f.getName(), " from NetworkTables.");
                    mLog.exception(e);
                } catch (Exception e) {
                    mLog.error("Could not load value of ", f.getName(), " from NetworkTables.");
                    mLog.exception(e);
                }
            }
        } 
    }

}
