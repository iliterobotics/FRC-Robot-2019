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

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

/**
 * Add your docs here.
 */
public abstract class NetworkTablesConstantsBase {

    private final ILog mLog = Logger.createLog(NetworkTablesConstantsBase.class);
    private final Field[] mDeclaredFields;
    private final Gson mGson;

    public NetworkTablesConstantsBase() {
        
        mDeclaredFields = this.getClass().getDeclaredFields();
        mGson = new Gson();
    }

    public void writeToNetworkTables() {
        NetworkTable table = NetworkTableInstance.getDefault().getTable(this.getClass().getSimpleName().toUpperCase());
        for (Field f : mDeclaredFields) {
            if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
                NetworkTableEntry entry = table.getEntry(f.getName());

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
        NetworkTable table = NetworkTableInstance.getDefault().getTable(this.getClass().getSimpleName().toUpperCase());
        for (Field f : mDeclaredFields) {
            NetworkTableEntry entry = table.getEntry(f.getName());
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
