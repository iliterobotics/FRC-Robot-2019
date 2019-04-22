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
import us.ilite.common.types.MatchMetadata;

public class CodexCsvLogger {

    private final ILog mLog = Logger.createLog(CodexCsvLogger.class);
    private static final String LOG_PATH_FORMAT = "/u/logs/%s/%s-%s-%s-%s.csv";
    private boolean mToTeleop;

    private Codex<?, ?> mCodex;
    private BufferedWriter writer;
    private MatchMetadata mMatchData;

    public CodexCsvLogger(Codex<?, ?> pCodex, MatchMetadata pMatchData) {
        mToTeleop = true;
        mCodex = pCodex;
        mMatchData = pMatchData;
        File autoFile = file(true);
        Data.handleCreation(autoFile);
        try {
            writer = new BufferedWriter(new FileWriter(autoFile));
        } catch (IOException pE) {
            pE.printStackTrace();
        }
    }

    public void writerToTeleop() {
        File teleFile = file(false);
        Data.handleCreation(teleFile);
        try {
            writer = new BufferedWriter(new FileWriter(teleFile));
        } catch (IOException pE) {
            pE.printStackTrace();
        }
        mLog.error("Switched to TELEOP writers");
        mToTeleop = false;
    }

    public boolean writeHeader() {
        boolean continueWriting = false;
        try {
            writer.append(mCodex.getCSVHeader());
            writer.newLine();
            continueWriting = true;
        } catch (Exception pE) {
            continueWriting = false;
        }
        return continueWriting;
    }

    public boolean writeLine() {
        boolean continueWriting = false;
        try {
            writer.append(mCodex.toCSV());
            writer.newLine();
            continueWriting = true;
        } catch (Exception pE) {
            continueWriting = false;
        }
        return continueWriting;
    }

    public File file(boolean isAuto) {
        String mEventName;
        String mMatchType;
        Integer mMatchNumber;
        String mDriveType = isAuto == true ? "Autonomous" : "Teleop";
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
        File file = new File(String.format( LOG_PATH_FORMAT,
                            mCodex.meta().getEnum().getSimpleName(),
                            mEventName,
                            mMatchType,
                            Integer.toString(mMatchNumber),
                            mDriveType
                            ));

        mLog.error("Creating log file at ", file.toPath());

        return file;
    }

    public void closeWriter() {
        if(mToTeleop) {
            writerToTeleop();
        } else if (writer != null) {
            try {
                writer.close();
            } catch (IOException pE) {
                pE.printStackTrace();
            }
        }
    }
}