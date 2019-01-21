package us.ilite.robot.modules;

import java.util.Optional;

import com.team254.lib.geometry.Translation2d;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import us.ilite.common.config.SystemSettings;

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

    public void setSnapshot(boolean snapshot) {
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

    /**
     * Calculate the distance to the currently tracked target.
     * @param targetHeight
     * @return Distance to target
     */
    public double calcTargetDistance( double targetHeight ) {
        // d = h/(tan(Ac - ty)) - db
        // hc = measured height of camera lens: SystemSettings.llCameraHeightIn
        // ht = height of the target being tracked: targetHeight
        // h = hc - ht = Height of triangle for distance calculation
        // d = distance from robot bumper to center of target bottom, this is what we're calculating
        // db = measured distance from camera lens to robot bumper: SystemSettings.llCameraToBumperIn
        // Ac = camera angle needed for calculating the distance: SystemSettings.llCameraAngleDeg
        // ty = Vertical Offset From Crosshair To Target (-20.5 degrees to 20.5 degrees) parameter from the limelight

        // we read the lime light values from mCurrentTarget, but this may be null if update is not
        // called for the first time

        double d = -1.0; // < 0 for error  TODO throw an execption???

        if (this.mCurrentTarget != null) {
            d = (SystemSettings.llCameraHeightIn - targetHeight) / 
                Math.tan( SystemSettings.llCameraAngleDeg - this.mCurrentTarget.ty ) - 
                SystemSettings.llCameraToBumperIn;
        }

        return d;
    }

    /**
     * Calculate the distance to the currently tracked target by target type
     * @param target
     * @return Distance to target
     */
    public double calcTargetDistance( SystemSettings.VisionTarget target ) {
        return this.calcTargetDistance( target.getHeight() );
    }


    /**
     * Calculate the approach angle to the currently tracked target.
     * A value of 0 deg means we are perpendicular to the target,
     * A negative angle means the robot is to the left of the target
     * A positive angle means the robot is to the right of the target
     * @return Approach angle to target
     */
    public double calcTargetApproachAngle() {
        // ts = ts angle or skew parameter from the limelight

        // we read the lime light values from mCurrentTarget, but this may be null if update is not
        // called for the first time

        double approachAngle = 0.0;  
        
        // TODO throw an execption on error???
        if (this.mCurrentTarget != null) {
            // For -90 < Ts < -45 use the right hand function. For -45 < Ts <= 0 use the left hand function.


            // get the skew angle and figure out which conversion to use
            double ts = this.mCurrentTarget.ts;

            if ( ts <= 0.0 && ts > -45.0 ) {
                // left hand angle
                approachAngle = - SystemSettings.llLeftACoeff + ts*SystemSettings.llLeftBCoeff + ts*Math.pow(SystemSettings.llLeftCCoeff, 2.0);
            }
            // TODO should we verify the  -90 < Ts < -45 for right hand angles and throw an exception if we don't meet it?
            else { 
                // right hand angle
                approachAngle = SystemSettings.llRightACoeff + ts*SystemSettings.llRightBCoeff + ts*Math.pow(SystemSettings.llRightCCoeff, 2.0);
            }

        }

        return approachAngle;
    }

    /**
     * Find the target as point (x,y) in front of the robot
     * Returns (-1,-1) to indicate an error
     * @param target
     * @return
     *  The target location. The optional will be empty if there was an error
     */
    public Optional<Translation2d>  calcTargetLocation( SystemSettings.VisionTarget target ) {
        

        double distance = this.calcTargetDistance( target );
        double angle = this.calcTargetApproachAngle();

        // is target to the left of the robot?
        boolean bLeft = ( angle < 0 );

        angle = Math.abs(angle);
        
        if ( distance < 0.0 ) {
            return Optional.empty();
        }

        // Calculate X with correct sign, negative if target is to the left of the robot
        double x = distance * Math.sin( angle ) * ( bLeft ? -1.0 : 1.0 );
        double y = distance * Math.cos( angle );

        return Optional.of(new Translation2d(x,y));
    }



}