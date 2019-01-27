/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package us.ilite.common.lib.util;

import java.lang.reflect.Field;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import us.ilite.common.config.SystemSettings;

/**
 * Add your docs here.
 */
public abstract class NetworkTablesConstantsBase {

    private static final NetworkTableInstance kNetworkTableInstance = NetworkTableInstance.getDefault();

    private final ILog mLog = Logger.createLog(NetworkTablesConstantsBase.class);
    private final NetworkTable mTable;
    private final Field[] mDeclaredFields;
    private final Gson mGson;

    public NetworkTablesConstantsBase() {
        mTable = kNetworkTableInstance.getTable(this.getClass().getSimpleName().toUpperCase());
        mDeclaredFields = this.getClass().getDeclaredFields();
        mGson = new Gson();
    }

    public void writeToNetworkTables() {
        for (Field f : mDeclaredFields) {
            if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
                NetworkTableEntry entry = mTable.getEntry(f.getName());

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
            NetworkTableEntry entry = mTable.getEntry(f.getName());
            if(entry.exists()) {
                try {
                    Object value = mGson.fromJson(entry.getString(""), f.getGenericType());
                    f.set(this, value);
                    mLog.info("Successfully set ", f.getName(), " to ", value);
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
