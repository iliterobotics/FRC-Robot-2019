package us.ilite.robot;

import java.io.File;
import java.nio.file.Files;

import edu.wpi.first.wpilibj.Notifier;
import us.ilite.common.Data;
import us.ilite.common.config.SystemSettings;

public class CSVLogger implements Runnable {
    private Notifier mLoggingNotifier;
    private Data mData;
    private boolean mShouldContinue;

    public CSVLogger( Data pData ) {
        mData = pData;
        mLoggingNotifier = new Notifier( this );
        mShouldContinue = false;
    }

    /**
     * Starts the periodically called logging by mLoggingNotifier
     */
    public void start() {
        mShouldContinue = mData.logFromCodexToCSVHeader();
        mLoggingNotifier.startPeriodic( SystemSettings.kCSVLoggingPeriod );
    }

    /**
     * Stops the periodically called logging by mLoggingNotifier
     */
    public void stop() {
        mLoggingNotifier.stop();
    }

    public void run() {
        if(!mShouldContinue) {
            stop();
        } else {
            mShouldContinue = mData.logFromCodexToCSVLog();
        }
    }

}