package us.ilite.robot.commands;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import us.ilite.robot.modules.Limelight;
import us.ilite.robot.modules.Limelight.LedMode;

public class LimeLightBlink implements ICommand{
    private final NetworkTable mTable = NetworkTableInstance.getDefault().getTable("limelight");

    private Limelight mLimelight;
    private LedMode mPreviousLedMode, mCurrentLedMode;
    

    private double mPreviousTime;
    private double mTime;

    public LimeLightBlink(Limelight pLimelight, double pTime) {
        this.mLimelight = pLimelight;
        this.mTime = pTime;
    }

    @Override
    public void init(double pNow) {
        SmartDashboard.putBoolean("Initializing Command", true);

        mPreviousLedMode = LedMode.fromOrdinal(mTable.getEntry("ledMode").getNumber(0).intValue());

        this.mPreviousTime = pNow;
    }

    @Override
    public boolean update(double pNow) {
        SmartDashboard.putBoolean("Initializing Command", false);

        mCurrentLedMode = LedMode.fromOrdinal(mTable.getEntry("ledMode").getNumber(0).intValue());

        if(!mCurrentLedMode.equals(LedMode.LED_BLINK)) {
            mLimelight.setLedMode(LedMode.LED_BLINK);
        }

        if (pNow - mPreviousTime > mTime) {
            mLimelight.setLedMode(mPreviousLedMode);
            return true;
        }

        return false;
    }

    @Override
    public void shutdown(double pNow) {
        
    }
}

