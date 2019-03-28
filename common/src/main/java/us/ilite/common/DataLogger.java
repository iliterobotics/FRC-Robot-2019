package us.ilite.common;

import edu.wpi.first.networktables.NetworkTableInstance;

public class DataLogger extends Thread {
    //This is for logging codexes to csv
    public static void main(String[] args) {
        NetworkTableInstance.getDefault().startClientTeam(1885);
        Data loggedData = new Data();
        // loggedData.closeWriters();
        Thread logger = new Thread() {
            public void run() {
                boolean running = true;
                loggedData.logFromCodexToCSVHeader();
                    while(running) {
                        loggedData.logFromNetworkTables();
                        // System.out.println(System.currentTimeMillis() + " " + loggedData.imu);
                        loggedData.networkTablesCodexToCSVLog();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
            }
        };
        logger.start();
    }
}