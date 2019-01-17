package us.ilite.robot.modules;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;

public class Limelight extends Module {

    private final NetworkTable mTable = NetworkTableInstance.getDefault().getTable("limelight");
    public TargetingData mCurrentTarget = null;

    @Override
    public void modeInit(double pNow) {

    }

    @Override
    public void periodicInput(double pNow) {

    }

    @Override
    public void update(double pNow) {
        mCurrentTarget = new TargetingData();
    }

    @Override
    public void shutdown(double pNow) {

    }

    public enum LedMode {
        NO_CHANGE,
        LED_OFF,
        LED_BLINK,
        LED_ON;
    }

    public enum Stream {
        STANDARD,
        PIP_MAIN,
        PIP_SECONDARY;
    }

    public void setCamMode(boolean pMode) {
        mTable.getEntry("camMode").setBoolean(pMode);
    }

    public void setLedMode(LedMode pMode) {
        mTable.getEntry("ledMode").setNumber(pMode.ordinal());
    }

    public void setPipeline(int pipeline) {
        mTable.getEntry("pipeline").setNumber(pipeline);
    }

    public void setSnapshop(boolean snapshot) {
        mTable.getEntry("snapshot").setBoolean(snapshot);
    }

    public void setStream(Stream stream) { 
        mTable.getEntry("stream").setNumber(stream.ordinal());
    }

    public class TargetingData {
        public final boolean tv;
        public final double tx, ty, ta, ts, tl, tshort, tlong, thoriz, tvert;

        public TargetingData() {
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


}