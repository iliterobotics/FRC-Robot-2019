package us.ilite.robot.modules;

import java.util.function.Function;
import java.util.Optional;

import com.team254.lib.geometry.Translation2d;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.config.SystemSettings.VisionTarget;
import us.ilite.robot.modules.targetData.ITargetDataProvider;

public class Limelight extends Module implements ITargetDataProvider{

    private final NetworkTable mTable = NetworkTableInstance.getDefault().getTable("limelight");
    public ITargetingData mCurrentTarget = null;

    @Override
    public void modeInit(double pNow) {

    }

    @Override
    public void periodicInput(double pNow) {

    }

    @Override
    public void update(double pNow) {
        mCurrentTarget = new LimeLightTargetingData(mTable);
    }

    @Override
    public Optional<ITargetingData> getTargetingData() {
        return Optional.of(new LimeLightTargetingData(mTable));
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
                Math.tan( SystemSettings.llCameraAngleDeg - this.mCurrentTarget.getTy() ) - 
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
            double ts = this.mCurrentTarget.getTs();

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

    public Optional<Translation2d> calcTargetLocation(SystemSettings.VisionTarget target) {
        return calcTargetLocation(target, this::calcTargetDistance, (v)->this.calcTargetApproachAngle());
    }

    /**
     * Find the target as point (x,y) in front of the robot
     * Returns (-1,-1) to indicate an error
     * @param target the target to look form
     * @param distanceCalculator the calculating method used to calculate the distance of the target
     * @param approachAngleCalculator the calculating method used to calculate the approach angle of 
     * the target.
     * @return
     *  The target location. The optional will be empty if there was an error
     */
    protected Optional<Translation2d>  calcTargetLocation( SystemSettings.VisionTarget target, 
        Function<VisionTarget,Double>distanceCalculator, Function<Void,Double> approachAngleCalculator)
    {
        double distance = distanceCalculator.apply(target);
        if ( distance < 0.0 ) {
            return Optional.empty();
        }
        double angle = approachAngleCalculator.apply(null);

        // is target to the left of the robot?
        boolean bLeft = ( angle < 0 );

        angle = Math.abs(angle);

        // Calculate X with correct sign, negative if target is to the left of the robot
        double x = distance * Math.sin( angle ) * ( bLeft ? -1.0 : 1.0 );
        double y = distance * Math.cos( angle );

        return Optional.of(new Translation2d(x,y));
    }



}