package us.ilite.common;

import com.flybotix.hfr.codex.Codex;
import com.flybotix.hfr.codex.CodexSender;
import com.flybotix.hfr.io.MessageProtocols;
import com.flybotix.hfr.io.sender.ISendProtocol;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.io.CodexNetworkTables;
import us.ilite.common.io.CodexNetworkTablesParser;
import us.ilite.common.io.CodexParser;
import us.ilite.common.lib.util.SimpleNetworkTable;
import us.ilite.common.types.ETargetingData;
import us.ilite.common.types.drive.EDriveData;
import us.ilite.common.types.input.EDriverInputMode;
import us.ilite.common.types.input.ELogitech310;
import us.ilite.common.types.manipulator.ECargoSpit;
import us.ilite.common.types.manipulator.EElevator;
import us.ilite.common.types.manipulator.EIntake;
import us.ilite.common.types.sensor.EGyro;
import us.ilite.common.types.EFourBarData;
import us.ilite.common.types.sensor.EPowerDistPanel;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class Data {

    public CodexNetworkTables mCodexNT = CodexNetworkTables.getInstance();
    private final ILog mLogger = Logger.createLog(Data.class);
    
    //Add new codexes here as we need more

    public final Codex<Double, EGyro> imu = Codex.of.thisEnum(EGyro.class);
    public final Codex<Double, EDriveData> drive = Codex.of.thisEnum(EDriveData.class);
    public final Codex<Double, ELogitech310> driverinput = Codex.of.thisEnum(ELogitech310.class);
    public final Codex<Double, ELogitech310> operatorinput = Codex.of.thisEnum(ELogitech310.class);
    public final Codex<Double, EElevator> elevator = Codex.of.thisEnum(EElevator.class);
    public final Codex<Double, EFourBarData> fourbar = Codex.of.thisEnum(EFourBarData.class);
    public final Codex<Double, ECargoSpit> cargospit = Codex.of.thisEnum( ECargoSpit.class );
    public final Codex<Double, EPowerDistPanel> pdp = Codex.of.thisEnum(EPowerDistPanel.class);
    public final Codex<Double, EIntake> intake = Codex.of.thisEnum(EIntake.class);
    public Codex<Double, ETargetingData> limelight = Codex.of.thisEnum(ETargetingData.class);

    private final List<CodexSender> mSenders = new ArrayList<>();

    public final Codex[] mAllCodexes = new Codex[] {
            imu, /*drive,*/ driverinput, operatorinput, elevator, cargospit, pdp, intake, limelight, fourbar
    };

    public final Codex[] mLoggedCodexes = new Codex[] {
        imu, drive, driverinput, /*operatorinput,*/ elevator, cargospit,  pdp, intake, limelight, fourbar
    };

    public final Codex[] mCSVCodexes = new Codex[] {
        imu, drive, driverinput, operatorinput, elevator, cargospit, pdp
    };

    public final Codex[] mDisplayedCodexes = new Codex[] {
            imu, /*drive,*/ driverinput, operatorinput, elevator, cargospit, pdp
    };

    public static NetworkTableInstance kInst = NetworkTableInstance.getDefault();
    public static SimpleNetworkTable kLoggingTable = new SimpleNetworkTable("LoggingTable");
    public static SimpleNetworkTable kSmartDashboard = new SimpleNetworkTable("SmartDashboard");
    public static NetworkTable kLimelight = kInst.getTable("limelight");
    public static NetworkTable kAutonTable = kInst.getTable("AUTON_SELECTION");
    public static SimpleNetworkTable kDriverControlSelection = new SimpleNetworkTable("DriverControlSelection") {
        @Override
        public void initKeys() {
            getInstance().getEntry(EDriverInputMode.class.getSimpleName()).setDefaultNumber(-1);
        }
    };

    //Stores writers per codex needed for CSV logging
    private Map<String, Writer> mWriters = new HashMap<String, Writer>();
    private Map<String, Writer> mCodexWriters = new HashMap<String, Writer>();

    private List<CodexNetworkTablesParser<?>> mParsers;
    private List<CodexParser> mCodexParsers;

    /**
     * Create a Data object based on whether or not it is being used for logging
     * @param pLogging
     */
    public Data(boolean pLogging) {
        if(pLogging) {
            initParsers();
            handleCodexWriterCreation();
            handleNetworkTableWriterCreation();
        }
    }

    public Data() {
        this(true);
    }

    private void initParsers() {
        //Add new codexes as we support more into this list
        mParsers = Arrays.asList(
            new CodexNetworkTablesParser<EGyro>(imu),
            new CodexNetworkTablesParser<EDriveData>(drive),
            new CodexNetworkTablesParser<ELogitech310>(driverinput, "DRIVER"),
            new CodexNetworkTablesParser<ELogitech310>(operatorinput, "OPERATOR"),
            new CodexNetworkTablesParser<EElevator>( elevator, "ELEVATOR" ),
            new CodexNetworkTablesParser<ECargoSpit>( cargospit, "CARGOSPIT" ),
            new CodexNetworkTablesParser<EPowerDistPanel>( pdp, "PDP" ),
            new CodexNetworkTablesParser<EFourBarData>(fourbar, "FOURBAR")
        );
        
        mCodexParsers = Arrays.asList(
            new CodexParser(imu),
            new CodexParser(drive),
            new CodexParser(driverinput),
            new CodexParser(operatorinput),
            new CodexParser(elevator),
            new CodexParser(cargospit),
            new CodexParser(pdp)
        );
    }

    /**
     * Translate NT to on-computer codex for each CodexNetworkTablesParser in mParsers
     */
    public void logFromNetworkTables() {
        mParsers.forEach(c -> c.parseFromNetworkTables());
    }

    /**
     * Logs csv headers to the files using network tables
     * -- This should be called once before csv logging --
     */
    public void networkTablesCodexToCSVHeader() {
        for (CodexNetworkTablesParser<?> parser : mParsers) {
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
     * Logs codex values to its corresponding csv using network tables
     */
    public void networkTablesCodexToCSVLog() {
        for (CodexNetworkTablesParser<?> parser : mParsers) {
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

    public void logFromCodexToCSVHeader() {
        for ( CodexParser parser : mCodexParsers ) {
            
            try {
                Writer logger = mCodexWriters.get( parser.getWriterKey() );
                logger.append(parser.codexToCSVHeader());
                logger.flush();
            } catch ( IOException e ) {
                e.printStackTrace();
            }
        }
    
    }
    public void logFromCodexToCSVLog() {
        for (CodexParser parser : mCodexParsers ) {
            try {
                Writer logger = mCodexWriters.get(parser.getWriterKey());
                System.out.println(parser.getWriterKey() + "__-_-_-__-__--_-_-_" + mCodexWriters.get(parser.getWriterKey()));
                logger.append(parser.codexToCSVLog());
                logger.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Creates writers if they don't already exist
     */
    public void handleNetworkTableWriterCreation() {
        for (CodexNetworkTablesParser<?> parser : mParsers) {
            try {
                File file = parser.file();
                handleCreation(file);
                mWriters.put(parser.getCSVIdentifier(), new BufferedWriter(new FileWriter(parser.file())));
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleCodexWriterCreation() {
        for ( CodexParser parser : mCodexParsers ) {
            try {
                File file = parser.file();
                handleCreation( file );
                System.out.println("_____________________________________" + parser.getWriterKey());
                mCodexWriters.put( parser.getWriterKey(), new BufferedWriter( new FileWriter( parser.file() ) ) );
            } catch ( IOException e ) {
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

        for ( Writer writer : mCodexWriters.values() ) {
            try {
                writer.close();
            } catch ( IOException e ) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Makes the log file if it doesn't already exist
     */
    private void handleCreation(File pFile) {
        //Makes every folder before the file if the CSV's parent folder doesn't exist
        if(Files.notExists(pFile.toPath())) {
            pFile.getAbsoluteFile().getParentFile().mkdirs();
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
     * Sends the codexes across the network to the IP's found when the DS connected.
     */
    public void sendCodices() {
        for(CodexSender cs : mSenders) {
            for(Codex<?, ?> c : mDisplayedCodexes) {
                cs.sendIfChanged(c);
            }
        }
    }

    public void sendCodicesToNetworkTables() {
        for(Codex<?, ?> c : mLoggedCodexes) {
            mCodexNT.send(c);
        }
    }

    /**
     * @Deprecated
     * Do this before sending codices to NetworkTables
     */
    public void registerCodices() {
        for(Codex<?, ?> c : mLoggedCodexes) {
            mCodexNT.registerCodex(c);
        }
    }

    /**
     * Initializes the codex sender to the IP's registered with the robot connected to the DS.  If
     * an IP is expected but not found, reboot the RIO or restart the DS software.  This will transmit
     * via UDP to the IP's on a port set by <code>SystemSettings.sCODEX_COMMS_PORT</code>
     * @param pIpAddresses List of all IP's to send Codexes to.
     */
    public void initCodexSender(List<String> pIpAddresses) {
        for(String ip : pIpAddresses) {
            mLogger.warn("======> Initializing sender to " + ip + ":" + SystemSettings.sCODEX_COMMS_PORT);
            ISendProtocol protocol = MessageProtocols.createSender(MessageProtocols.EProtocol.UDP, SystemSettings.sCODEX_COMMS_PORT, SystemSettings.sCODEX_COMMS_PORT, ip);
            CodexSender sender = new CodexSender(protocol);
            mSenders.add(sender);
        }
    }
}