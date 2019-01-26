package us.ilite.common;

import java.util.Arrays;
import java.util.List;

import com.flybotix.hfr.codex.Codex;

import edu.wpi.first.networktables.NetworkTableInstance;
import us.ilite.common.io.CodexNetworkTablesParser;
import us.ilite.common.types.drive.EDriveData;
import us.ilite.common.types.input.ELogitech310;
import us.ilite.common.types.sensor.EGyro;

public class LoggedData {

    public Codex<Double, EGyro> imu = Codex.of.thisEnum(EGyro.class);
    public Codex<Double, EDriveData> drive = Codex.of.thisEnum(EDriveData.class);
    public Codex<Double, ELogitech310> driverinput = Codex.of.thisEnum(ELogitech310.class);
    public Codex<Double, ELogitech310> operatorinput = Codex.of.thisEnum(ELogitech310.class);
    
    public List<CodexNetworkTablesParser> loggedCodexes = Arrays.asList(
        new CodexNetworkTablesParser<EGyro>(imu),
        new CodexNetworkTablesParser<EDriveData>(drive),
        new CodexNetworkTablesParser<ELogitech310>(driverinput, "DRIVER"),
        new CodexNetworkTablesParser<ELogitech310>(operatorinput, "OPERATOR")
    );

    /**
     * Translate NT to on-computer codex for each CodexNetworkTablesParser in loggedCodexes
     */
    public void logFromNetworkTables() {
        loggedCodexes.forEach(c -> c.parseFromNetworkTables());
    }

    /**
     * Makes a csv file and writes the starting row/header for each CodexNetworkTablesParser in loggedCodexes
     */
    public void logFromCodexToCSVHeader() {
        loggedCodexes.forEach(c -> c.codexToCSVHeader());
    }
    /**
     * Logs codex values to its corresponding csv
     */
    public void logFromCodexToCSVLog() {
        loggedCodexes.forEach(c -> c.codexToCSVLog());
    }

    //For testing purposes
    public static void main(String[] args) {
        
        NetworkTableInstance.getDefault().startClientTeam(1885);
        LoggedData loggedData = new LoggedData();

        Thread logger = new Thread() {
            public void run() {
                loggedData.logFromNetworkTables();
                loggedData.logFromCodexToCSVHeader();
                    while(true) {
                        loggedData.logFromNetworkTables();
                        // System.out.println(System.currentTimeMillis() + " " + loggedData.imu);
                        loggedData.logFromCodexToCSVLog();
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
            }
        };

        logger.start();

        //Offline Testing
        // NetworkTableInstance kInst = NetworkTableInstance.getDefault();
        // kInst.startServer();
        // kInst.startClient("localhost");
        // LoggedData loggedData = new LoggedData();
        // Thread logger = new Thread() {
        //     public void run() {
        //             while(true) {
        //                 loggedData.logFromNetworkTables();
        //                 // System.out.println(System.currentTimeMillis() + " " + loggedData.imu);
        //                 try {
        //                     Thread.sleep(100);
        //                 } catch (InterruptedException e) {
        //                     // TODO Auto-generated catch block
        //                     e.printStackTrace();
        //                 }
        //             }
        //     }
        // };

        // logger.start();

    }

}