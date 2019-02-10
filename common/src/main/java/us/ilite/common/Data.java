package us.ilite.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.flybotix.hfr.codex.Codex;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import us.ilite.common.io.CodexNetworkTables;
import us.ilite.common.io.CodexNetworkTablesParser;
import us.ilite.common.lib.util.SimpleNetworkTable;
import us.ilite.common.types.ETargetingData;
import us.ilite.common.types.drive.EDriveData;
import us.ilite.common.types.input.EDriverInputMode;
import us.ilite.common.types.input.ELogitech310;
import us.ilite.common.types.sensor.EGyro;

public class Data {

    public CodexNetworkTables mCodexNT = CodexNetworkTables.getInstance();
    
    //Add new codexes here as we need more
    public final Codex<Double, EGyro> imu = Codex.of.thisEnum(EGyro.class);
    public final Codex<Double, EDriveData> drive = Codex.of.thisEnum(EDriveData.class);
    public final Codex<Double, ELogitech310> driverinput = Codex.of.thisEnum(ELogitech310.class);
    public final Codex<Double, ELogitech310> operatorinput = Codex.of.thisEnum(ELogitech310.class);
    public Codex<Double, ETargetingData> limelight = Codex.of.thisEnum(ETargetingData.class);

    public final Codex[] mLoggedCodexes = new Codex[] {
        imu, drive, driverinput, operatorinput
    };

    public static NetworkTableInstance kInst = NetworkTableInstance.getDefault();
    public static SimpleNetworkTable kLoggingTable = new SimpleNetworkTable("LoggingTable");
    public static SimpleNetworkTable kSmartDashboard = new SimpleNetworkTable("SmartDashboard");
    public static NetworkTable kLimelight = kInst.getTable("limelight");
    public static SimpleNetworkTable kDriverControlSelection = new SimpleNetworkTable("DriverControlSelection") {
        @Override
        public void initKeys() {
            getInstance().getEntry(EDriverInputMode.class.getSimpleName()).setDefaultNumber(-1);
        }
    };

    //Stores writers per codex needed for CSV logging
    private Map<String, Writer> mWriters = new HashMap<String, Writer>();
    private boolean mHasMadeWriters = false;

    private List<CodexNetworkTablesParser> mParsers;

    public Data() {
        //Add new codexes as we support more into this list
        mParsers = Arrays.asList(
            new CodexNetworkTablesParser<EGyro>(imu),
            new CodexNetworkTablesParser<EDriveData>(drive),
            new CodexNetworkTablesParser<ELogitech310>(driverinput, "DRIVER"),
            new CodexNetworkTablesParser<ELogitech310>(operatorinput, "OPERATOR")
        );

    }

    /**
     * Translate NT to on-computer codex for each CodexNetworkTablesParser in mParsers
     */
    public void logFromNetworkTables() {
        mParsers.forEach(c -> c.parseFromNetworkTables());
    }

    /**
     * Makes a csv file and writes the starting row/header for each CodexNetworkTablesParser in mParsers
     */
    public void logFromCodexToCSVHeader() {
        for (CodexNetworkTablesParser parser : mParsers) {
            try {
                Writer logger = mWriters.get(parser.getCSVIdentifier());
                logger.append(parser.codexToCSVHeader());
                logger.flush();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * Logs codex values to its corresponding csv
     */
    public void logFromCodexToCSVLog() {


        if(!mHasMadeWriters) {
            //This loop makes a Writer for each parser and sticks it into mWriters
            for (CodexNetworkTablesParser parser : mParsers) {
                try {
                    File file = parser.file();
                    handleCreation(file);
                    mWriters.put(parser.getCSVIdentifier(), new BufferedWriter(new FileWriter(parser.file())));
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mHasMadeWriters = true;
        }

        for (CodexNetworkTablesParser parser : mParsers) {
            try {
                Writer logger = mWriters.get(parser.getCSVIdentifier());
                logger.append(parser.codexToCSVLog());
                logger.flush();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * Closes all the writers in mWriters
     */
    public void closeWriters() {
        for (Writer writer : mWriters.values()) {
            try {
                writer.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Makes the log file if it doesn't already exist
     */
    private void handleCreation(File pFile) {
        //Makes every folder before the file if the CSV's parent folder doesn't exist
        if(!pFile.getParentFile().exists()) {
            pFile.getParentFile().mkdirs();
        }

        //Creates the .CSV if it doesn't exist
        if(!pFile.exists()) {
            try {
                pFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sends Codex entries into its corresponding NetworkTable
     */
    public void sendCodices() {
        mCodexNT.send(imu);
        mCodexNT.send(drive);
        mCodexNT.send("DRIVER", driverinput);
        mCodexNT.send("OPERATOR", operatorinput);
    }

    /**
     * Do this before sending codices to NetworkTables
     */
    public void registerCodices() {
        mCodexNT.registerCodex(EGyro.class);
        mCodexNT.registerCodex(EDriveData.class);
        mCodexNT.registerCodex("DRIVER", ELogitech310.class);
        mCodexNT.registerCodex("OPERATOR", ELogitech310.class);
    }
}