package us.ilite.robot.commands;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import us.ilite.common.config.SystemSettings;
import us.ilite.robot.modules.Limelight;
import us.ilite.robot.modules.Limelight.LedMode;

public class LimelightBlink implements ICommand{
    private final NetworkTable mTable = NetworkTableInstance.getDefault().getTable("limelight");

    private Limelight mLimelight;
    private LedMode mPreviousLedMode, mCurrentLedMode;
    

    private double mPreviousTime;
    private double mTime = SystemSettings.kLimelightBlinkPeriod;
    private boolean isBlinking;

    public LimelightBlink(Limelight pLimelight) {
        this.mLimelight = pLimelight;
    }

    @Override
    public void init(double pNow) {
        SmartDashboard.putBoolean("Initializing Command", true);

        mPreviousLedMode = LedMode.fromOrdinal(mTable.getEntry("ledMode").getNumber(0).intValue());

        this.mPreviousTime = pNow;

        isBlinking = false;
    }

    @Override
    public boolean update(double pNow) {
        SmartDashboard.putBoolean("Initializing Command", false);

        if (isBlinking && pNow - mPreviousTime > mTime) {
            stop();
            return true;
        }
        
        mPreviousTime = pNow;
        return false;
    }

    public void blink() {
        mPreviousLedMode = LedMode.fromOrdinal(mTable.getEntry("ledmode").getNumber(0).intValue());
        mLimelight.setLedMode(LedMode.LED_BLINK);
        isBlinking = true;
    }

    public void stop() {
        mLimelight.setLedMode(LedMode.LED_ON);
        isBlinking = false;
    }

    @Override
    public void shutdown(double pNow) {
        
    }
}

