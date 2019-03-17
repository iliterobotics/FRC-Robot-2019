package us.ilite.common.io;

import java.io.File;

import com.flybotix.hfr.codex.Codex;

import edu.wpi.first.wpilibj.DriverStation;

public class CodexParser {
    private static final String LOG_PATH_FORMAT = "/u/logs/%s/%s-%s-%s.csv";
    private Codex<?, ?> mCodex;
    private String mWriterKey;

    public CodexParser( Codex<?, ?> pCodex ) {
        mCodex = pCodex;
        constructKey( mCodex );
    }

    public void constructKey( Codex<?, ?> constructFrom ) {
        mWriterKey = constructFrom.meta().getEnum().getSimpleName();
    }

    public String codexToCSVHeader() {
        return mCodex.getCSVHeader();
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
                        Integer.toString(DriverStation.getInstance().getMatchNumber()),
                        mCodex.meta().getEnum().getSimpleName()
                        ));
    }

    public String getWriterKey() {
        return mWriterKey;
    }
}