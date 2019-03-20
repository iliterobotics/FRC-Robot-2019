package us.ilite.common.io;

import java.io.File;
import java.nio.file.Files;

import com.flybotix.hfr.codex.Codex;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import edu.wpi.first.wpilibj.DriverStation;

public class CodexParser {

    private final ILog mLog = Logger.createLog(CodexParser.class);
    private static final String ROBOT_DIR = "/u";
    private static final String USER_DIR = System.getProperty("user.home");
    private static final String LOG_PATH_FORMAT = "/logs/%s/%s-%s-%s.csv";
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

        String dir = "";
        if(Files.notExists(new File("/u").toPath())) {
            dir = USER_DIR;
        } else {
            dir = ROBOT_DIR;
        }

        String eventName = DriverStation.getInstance().getEventName();
        if ( eventName.length() <= 0 ) {
            eventName = "Default-Event";
        }
        File file = new File(String.format( dir + LOG_PATH_FORMAT,
                            eventName,
                            DriverStation.getInstance().getMatchType().name(),
                            Integer.toString(DriverStation.getInstance().getMatchNumber()),
                            mCodex.meta().getEnum().getSimpleName()
                            ));

        mLog.error("Creating log file at ", file.toPath());

        return file;
    }

    public String getWriterKey() {
        return mWriterKey;
    }
}