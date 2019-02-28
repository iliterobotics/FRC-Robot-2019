package us.ilite.robot;

import edu.wpi.first.wpilibj.Notifier;
import us.ilite.common.Data;
import us.ilite.common.config.SystemSettings;

public class CSVLogger implements Runnable {
    private Notifier mLoggingNotifier;
    private Data mData;

    public CSVLogger( Data pData ) {
        mData = pData;
        mLoggingNotifier = new Notifier( this );
    }

    /**
     * Starts the periodically called logging by mLoggingNotifier
     */
    public void start() {
        mLoggingNotifier.startPeriodic( SystemSettings.kControlLoopPeriod );
        mData.logFromCodexToCSVHeader();
    }

    /**
     * Stops the periodically called logging by mLoggingNotifier
     */
    public void stop() {
        mLoggingNotifier.stop();
    }

    public void run() {
        mData.logFromCodexToCSVLog();
    }

}