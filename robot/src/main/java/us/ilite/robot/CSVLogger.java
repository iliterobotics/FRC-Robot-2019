package us.ilite.robot;

import java.io.IOException;
import java.io.Writer;

import edu.wpi.first.wpilibj.Notifier;
import us.ilite.common.Data;
import us.ilite.common.config.SystemSettings;

public class CSVLogger implements Runnable {
    private Notifier mLoggingNotifier;
    private Data mData;

    public CSVLogger( Data pData ) {
        mLoggingNotifier = new Notifier( this );
    }

    public synchronized void start() {
        mLoggingNotifier.startPeriodic( SystemSettings.kControlLoopPeriod );
        mData.logFromCodexToCSVHeader();
    }

    public synchronized void stop() {
        mData.closeWriters();
        mLoggingNotifier.stop();
    }

    public void run() {
        mData.logFromCodexToCSVLog();
    }

}