package us.ilite.common.io;

import java.io.File;

import com.flybotix.hfr.codex.Codex;
import com.flybotix.hfr.codex.CodexOf;

import edu.wpi.first.wpilibj.DriverStation;

public class CodexParser<E extends Enum<E> & CodexOf<Double>> {
    private static final String LOG_PATH_FORMAT = "C:\\Users\\jjohn\\Documents\\logging\\%1$s\\%2$s-%3$s.csv";
    // private static final String LOG_PATH_FORMAT = "/u/logs/%s/%s-%s.csv";
    private Codex<Double, E> mCodex;
    private String mWriterKey;

    public CodexParser( Codex<Double, E> pCodex ) {
        mCodex = pCodex;
        constructKey( mCodex.meta().getEnum() );
    }

    public void constructKey( Class<E> constructFrom ) {
        mWriterKey = constructFrom.getSimpleName().toUpperCase();
    }

    public String codexToCSVHeader() {
        return "String";
    }

    public String codexToCSVLog() {
        return mCodex.toCSV() + " : " + System.currentTimeMillis() / 1000;
    }

    public File file() {
        String eventName = DriverStation.getInstance().getEventName();
        if ( eventName.length() <= 0 ) {
            eventName = "Default-Event";
        }
        return new File(String.format( LOG_PATH_FORMAT, 
                        eventName,
                        DriverStation.getInstance().getMatchType().name(),
                        Integer.toString(DriverStation.getInstance().getMatchNumber())));
    }

    public String getWriterKey() {
        return mWriterKey;
    }
}