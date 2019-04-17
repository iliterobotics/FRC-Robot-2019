package us.ilite.robot;

import java.util.ArrayList;

import com.flybotix.hfr.codex.Codex;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

import edu.wpi.first.wpilibj.Notifier;
import us.ilite.common.Data;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.io.CodexCsvLogger;
import us.ilite.common.types.MatchMetadata;

public class CSVLogger implements Runnable {
    private ILog mLog = Logger.createLog(CSVLogger.class);
    private ArrayList<CodexCsvLogger> mCodexCsvLoggers;
    private MatchMetadata mMatchData;
    private Notifier mLoggingNotifier;
    private Data mData;
    private boolean mShouldContinue, mIsRunning;

    public CSVLogger(Data pData, MatchMetadata pMatchData) {
        mData = pData;
        mLoggingNotifier = new Notifier( this );
        mMatchData = pMatchData;
        mShouldContinue = false;
        mIsRunning = false;

        initParsers();
    }

    /**
     * Starts the periodically called logging by mLoggingNotifier
     */
    public void start() {
        if(!mIsRunning) {
            mShouldContinue = logFromCodexToCSVHeader();
            mLoggingNotifier.startPeriodic( SystemSettings.kCSVLoggingPeriod );
            mIsRunning = true;
        }
    }

    /**
     * Periodically called run for logging
     */
    public void run() {
        if(mIsRunning) {
            if(!mShouldContinue) {
                mLog.error("USB not found! Stopping logging.");
                stop();
            }
            mShouldContinue = logFromCodexToCSVLog();
        }
    }

    /**
     * Stops the periodically called logging by mLoggingNotifier
     */
    public void stop() {
        if(mIsRunning) {
            mLoggingNotifier.stop();
            closeWriters();
            mIsRunning = false;
        }
    }

    /**
     * Initialize parsers used for logging codexes
     */
    private void initParsers() {
        mCodexCsvLoggers = new ArrayList<>();
        for(Codex c : mData.mLoggedCodexes) mCodexCsvLoggers.add(new CodexCsvLogger(c, mMatchData));
    }

    /**
     * Log codex headers to csv
     * @return whether it can keep logging or not
     */
    public boolean logFromCodexToCSVHeader() {
        boolean keepLogging = false;
        try {
            for (CodexCsvLogger c : mCodexCsvLoggers) {
                keepLogging = c.writeHeader();
                if(!keepLogging) {
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println(e);
            keepLogging = false;
        }
        return keepLogging;
    }

    /**
     * Log codexes to CSV
     * @return whether it can keep logging or not
     */
    public boolean logFromCodexToCSVLog() {
        boolean keepLogging = false;
        try {
            for (CodexCsvLogger c : mCodexCsvLoggers) {
                keepLogging = c.writeLine();
                if(!keepLogging) {
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println(e);
            keepLogging = false;
        }
        return keepLogging;
    }

    /**
     * Close writers for leak prevent
     */
    public void closeWriters() {
        mCodexCsvLoggers.forEach(c -> c.closeWriter());
    }

}