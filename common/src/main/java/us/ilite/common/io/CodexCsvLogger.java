package us.ilite.common.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.flybotix.hfr.codex.Codex;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

import edu.wpi.first.wpilibj.DriverStation;
import us.ilite.common.Data;
import us.ilite.common.types.MatchMetadata;

public class CodexCsvLogger {

    private final ILog mLog = Logger.createLog(CodexCsvLogger.class);
    private static final String ROBOT_DIR = "/u";
    // private static final String USER_DIR = System.getProperty("user.home");
    private static final String LOG_PATH_FORMAT = "/logs/%s/%s-%s-%s.csv";
    private Codex<?, ?> mCodex;
    private BufferedWriter writer;
    private MatchMetadata mMatchData;

    public CodexCsvLogger(Codex<?, ?> pCodex, MatchMetadata pMatchData) {
        mCodex = pCodex;

        File file = file(false);
        Data.handleCreation( file );
        try {
            writer = new BufferedWriter( new FileWriter( file ) );
        } catch (IOException pE) {
            pE.printStackTrace();
        }

        mMatchData = pMatchData;

    }

    public boolean writeHeader() {
        try {
            writer.append(mCodex.getCSVHeader());
            writer.newLine();
        } catch (IOException pE) {
            pE.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean writeLine() {
        try {
            writer.append(mCodex.toCSV());
            writer.newLine();
        } catch (IOException pE) {
            pE.printStackTrace();
            return false;
        }
        return true;
    }

    public File file(boolean handleUSBConnection) {

        String dir = ROBOT_DIR;
        // if(!handleUSBConnection) {
        //     if(Files.notExists(new File("/u").toPath())) {
        //         dir = USER_DIR;
        //     } else {
        //         dir = ROBOT_DIR;
        //     }
        // } else {
        //     dir = USER_DIR;
        // }
        String mEventName;
        String mMatchType;
        Integer mMatchNumber;
        if(mMatchData != null) {
            mEventName = mMatchData.mEventName;
            mMatchType = mMatchData.mMatchType.toString();
            mMatchNumber = mMatchData.mMatchNumber;
        } else {
            mEventName = DriverStation.getInstance().getEventName();
            mMatchType = DriverStation.getInstance().getMatchType().toString();
            mMatchNumber = DriverStation.getInstance().getMatchNumber();
        }
        if ( mEventName.length() <= 0 ) {
            // event name format: MM-DD-YYYY_HH-MM-SS
            mEventName =  new SimpleDateFormat("MM-dd-YYYY_HH-mm-ss").format(Calendar.getInstance().getTime());
        }
        File file = new File(String.format( dir + LOG_PATH_FORMAT,
                            mCodex.meta().getEnum().getSimpleName(),
                            mEventName,
                            mMatchType,
                            Integer.toString(mMatchNumber)
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