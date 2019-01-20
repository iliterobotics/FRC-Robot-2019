/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package us.ilite.common.lib.util;

import java.lang.reflect.Field;

import com.google.gson.Gson;

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
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    System.err.println("Failed value write for " + entry.getName());
                }
            }
        }
    }

    public void loadFromNetworkTables() {
        for (Field f : mDeclaredFields) {
            NetworkTableEntry entry = mTable.getEntry(f.getName());
            if(entry.exists()) {
                try {
                    f.set(this, mGson.fromJson(entry.getString(""), f.getGenericType()));
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    // e.printStackTrace();
                    System.err.println("Failed value retrieval for " + entry.getName());
                }
            }
        } 
    }

}
