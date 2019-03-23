package us.ilite.common.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.flybotix.hfr.codex.Codex;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import edu.wpi.first.wpilibj.DriverStation;
import us.ilite.common.Data;

public class CodexCsvLogger {

    public static final String USB_DIR = "/u";
    public static final String USER_DIR = System.getProperty("user.home");
    private static final String LOG_PATH_FORMAT = "/logs/%s/%s-%s-%s.csv";

    private final ILog mLog = Logger.createLog(CodexCsvLogger.class);

    private Codex<?, ?> mCodex;
    private BufferedWriter writer;

    public CodexCsvLogger(Codex<?, ?> pCodex ) {
        mCodex = pCodex;

        File file = file();
        Data.handleCreation( file );
        try {
            writer = new BufferedWriter( new FileWriter( file ) );
        } catch (IOException pE) {
            pE.printStackTrace();
        }

    }

    public void writeHeader() {
        try {
            writer.append(mCodex.getCSVHeader());
            writer.newLine();
        } catch (IOException pE) {
            pE.printStackTrace();
        }
    }

    public void writeLine() {
        try {
            writer.append(mCodex.toCSV());
            writer.newLine();
        } catch (IOException pE) {
            pE.printStackTrace();
        }
    }

    public File file() {

        // Don't default to home dir to avoid filling up memory
//        String dir = "";
//        if(Files.notExists(new File(USB_DIR).toPath())) {
//            dir = USER_DIR;
//        } else {
//            dir = USB_DIR;
//        }

        String dir = USB_DIR;

        String eventName = DriverStation.getInstance().getEventName();
        if ( eventName.length() <= 0 ) {
            // event name format: MM-DD-YYYY_HH-MM-SS
            eventName =  new SimpleDateFormat("MM-dd-YYYY_HH-mm-ss").format(Calendar.getInstance().getTime());
        }

        File file = new File(String.format( dir + LOG_PATH_FORMAT,
                            eventName,
                            mCodex.meta().getEnum().getSimpleName(),
                            DriverStation.getInstance().getMatchType().name(),
                            Integer.toString(DriverStation.getInstance().getMatchNumber())
                            ));

        mLog.error("Creating log file at ", file.toPath());

        return file;
    }

    public void closeWriter() {
        try {
            writer.flush();
            writer.close();
        } catch (IOException pE) {
            pE.printStackTrace();
        }
    }

}