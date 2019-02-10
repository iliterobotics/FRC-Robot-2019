package us.ilite.common.types;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.DriverStation.MatchType;

public class MatchMetadata {

    public final String mEventName;
    public final int mLocation;
    public final Alliance mAlliance;
    public final MatchType mMatchType;
    public final int mReplayNumber;
    public final int mMatchNumber;
    public final int hash;

    public MatchMetadata() {
        
        if(DriverStation.getInstance().isFMSAttached()) {
            mEventName = DriverStation.getInstance().getEventName();
            mLocation = DriverStation.getInstance().getLocation();
            mAlliance = DriverStation.getInstance().getAlliance();
            mMatchType = DriverStation.getInstance().getMatchType();
            mReplayNumber = DriverStation.getInstance().getReplayNumber();
            mMatchNumber = DriverStation.getInstance().getMatchNumber();
            int i = 7;
            i += mEventName.hashCode();
            i += 7 * mMatchType.ordinal();
            i += 7 * mMatchNumber;
            hash = i;
        } else {
            mEventName = "Test";
            mLocation = 0;
            mAlliance = Alliance.Invalid;
            mMatchType = MatchType.None;
            mReplayNumber = 0;
            mMatchNumber = (int)(Math.random() * (double)Integer.MAX_VALUE);
            hash = mMatchNumber;
        }
    }
}