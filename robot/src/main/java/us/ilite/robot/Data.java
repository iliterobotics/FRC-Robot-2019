package us.ilite.robot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import com.flybotix.hfr.codex.Codex;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import us.ilite.common.LoggedData;
import us.ilite.common.io.CodexNetworkTables;
import us.ilite.common.types.drive.EDriveData;
import us.ilite.common.types.input.EDriverInputMode;
import us.ilite.common.types.input.ELogitech310;
import us.ilite.common.types.sensor.EGyro;
import us.ilite.lib.util.SimpleNetworkTable;

public class Data {

    private static final String LOG_PATH_FORMAT = "./logs/%s-log.csv";

    public LoggedData loggedData = new LoggedData();

    public CodexNetworkTables mCodexNT = CodexNetworkTables.getInstance();
    public Codex<Double, EGyro> imu = loggedData.imu;
    public Codex<Double, EDriveData> drive = loggedData.drive;
    public Codex<Double, ELogitech310> driverinput = Codex.of.thisEnum(ELogitech310.class);
    public Codex<Double, ELogitech310> operatorinput = Codex.of.thisEnum(ELogitech310.class);

    public static NetworkTableInstance inst = NetworkTableInstance.getDefault();
    public static SimpleNetworkTable kLoggingTable = new SimpleNetworkTable("LoggingTable");
    public static SimpleNetworkTable kSmartDashboard = new SimpleNetworkTable("SmartDashboard");
    public static NetworkTable kLimelight = inst.getTable("limelight");
    public static SimpleNetworkTable kDriverControlSelection = new SimpleNetworkTable("DriverControlSelection") {
        @Override
        public void initKeys() {
            getInstance().getEntry(EDriverInputMode.class.getSimpleName()).setDefaultNumber(-1);
        }
    };

    public Data() {
        registerCodices();
        sendCodices();
    }

    public void registerCodices() { //registers/makes codex table with 
        mCodexNT.registerCodex(EGyro.class);
        mCodexNT.registerCodex(EDriveData.class);
    }
    
    public void sendCodices() { //sends codex tables to NT
        mCodexNT.send(imu);
        mCodexNT.send(drive);
    }

    /**
     * Writes codex to CSV into the LOG_PATH_FORMAT path
     * @param cod codex to be manipulated
     * @param pLogName the log file name
     */
    public <E> void codexToCSV(Codex<Double, ?> cod, String pLogName) { //Fix generic/wildcard
        File log = new File(String.format(LOG_PATH_FORMAT, pLogName));
        try {
            handleCreation(log);
            Writer logger = new BufferedWriter(new FileWriter(log));
            logger.append(cod.getCSVHeader());
            logger.append("\n"+cod.toCSV());
            logger.close();
        }
        catch(IOException e) {
            System.err.printf("Error");
            e.printStackTrace();
        }
    }
    
    private void handleCreation(File pFile) throws IOException {
        if(pFile.getParentFile().exists()) pFile.getParentFile().mkdir();
        if(!pFile.exists()) pFile.createNewFile();
    }
}
