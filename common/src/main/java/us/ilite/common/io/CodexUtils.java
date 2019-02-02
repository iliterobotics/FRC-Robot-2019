
package us.ilite.common.io;

import edu.wpi.first.wpilibj.DriverStation;
import us.ilite.common.types.MatchMetadata;

public class CodexUtils {
    
    /**
     * @return a global id for codex.  It is based upon the current match, or a UUID if there is no match.
     * The result can be read as a string.
     */
    public static Integer getMatchGlobalId(MatchMetadata pMetadata) {
        if(DriverStation.getInstance().isFMSAttached()) {
            return new MatchMetadata().hashCode();
        }
        return 0;
    }
}