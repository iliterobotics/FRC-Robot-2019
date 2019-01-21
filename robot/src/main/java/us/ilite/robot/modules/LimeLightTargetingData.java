/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package us.ilite.robot.modules;

import edu.wpi.first.networktables.NetworkTable;

/**
 * Add your docs here.
 */
public class LimeLightTargetingData implements ITargetingData{
    private final boolean tv;
    private final double tx;
    private final double ty;
    private final double ta;
    private final double ts;
    private final double tl;
    private final double tshort;
    private final double tlong;
    private final double thoriz;
    private final double tvert;
    LimeLightTargetingData(NetworkTable mTable) {
        tv = mTable.getEntry("tv").getBoolean(false);
            tx = mTable.getEntry("tx").getDouble(Double.MIN_VALUE);
            ty = mTable.getEntry("ty").getDouble(Double.MIN_VALUE);
            ta = mTable.getEntry("ta").getDouble(Double.MIN_VALUE);
            ts = mTable.getEntry("ts").getDouble(Double.MIN_VALUE);
            tl = mTable.getEntry("tl").getDouble(Double.MIN_VALUE);
            tshort = mTable.getEntry("tshort").getDouble(Double.MIN_VALUE);
            tlong = mTable.getEntry("tlong").getDouble(Double.MIN_VALUE);
            thoriz = mTable.getEntry("thoriz").getDouble(Double.MIN_VALUE);
            tvert = mTable.getEntry("tvert").getDouble(Double.MIN_VALUE);
    }

    public double getTx() {
        return tx;
    }

    public double getTy(){
        return ty;
    }

    public double getTa(){
        return ta;
    }

    public boolean getTv() {
        return tv;
    }

    public double getTs(){
        return ts;
    }
    
    public double getTl(){
        return tl;
    }
    
    public double getTshort(){
        return tshort;
    }
    
    public double getTlong(){
        return tlong;
    }
    
    public double getTHoriz(){
        return thoriz;
    } 
    
    public double getTvert(){
        return tvert;
    }
    @Override
        public String toString() {
            return "{" +
                " tv='" + tv +
                ", tx='" + tx +
                ", ty='" + ty +
                ", ta='" + ta +
                ", ts='" + ts +
                ", tl='" + tl +
                ", tshort='" + tshort +
                ", tlong='" + tlong +
                ", thoriz='" + thoriz +
                ", tvert='" + tvert +
                "}";
        }
    }
