package us.ilite.common.io;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.flybotix.hfr.codex.Codex;
import com.flybotix.hfr.codex.CodexOf;
import com.flybotix.hfr.util.lang.EnumUtils;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class CodexNetworkTablesParser<E extends Enum<E> & CodexOf<Double>> {

    // private static final String LOG_PATH_FORMAT = System.getProperty("user.dir")+"\\logs\\%s\\%s.csv";
    private static final String LOG_PATH_FORMAT = "/u/logs/%s/%s-%s.csv";

    private final NetworkTableInstance kNetworkTablesInstance = NetworkTableInstance.getDefault();
    private final NetworkTable kNetworkTable;

    private Codex<Double, E> mCodex;
    private Class<E> mEnumClass;

    private String csvIdentifier;

    /**
     * Use this constructor when you have multiple codices that have the same enumeration
     * @param pCodex The codex that this CodexNetworkTablesParser instance is parsing
     * @param pNetworkTablesName Composite key for the NetworkTable
     */
    public CodexNetworkTablesParser(Codex<Double, E> pCodex, String pNetworkTablesName) {
        mCodex = pCodex;
        mEnumClass = mCodex.meta().getEnum(); //This gets the enumeration that corresponds to the codex
        csvIdentifier = constructNetworkTableName(mEnumClass, pNetworkTablesName);
        kNetworkTable = kNetworkTablesInstance.getTable(csvIdentifier);
    }

    /**
     * 
     * @param pCodex The codex that this CodexNetworkTablesParser instance is parsing
     */
    public CodexNetworkTablesParser(Codex<Double, E> pCodex) {
        mCodex = pCodex;
        mEnumClass = mCodex.meta().getEnum();
        csvIdentifier = constructNetworkTableName(mEnumClass);
        kNetworkTable = kNetworkTablesInstance.getTable(csvIdentifier);
    }

    /**
     * Gets the NetworkTable name of the codex in question when
     * there are multiple codex instances of the same enumeration class
     * @param pName Extra identification to find the NetworkTable that corresponds with the codex
     * @param pClass The enumeration that the codex is based on
     */
    public static <E extends Enum<E>> String constructNetworkTableName(Class<E> pClass, String pName) {
        return pName.toUpperCase() + "-" + constructNetworkTableName(pClass);
    }

    /**
     * Gets the NetworkTable name of the codex in question
     * @param pClass The codex's enum class
     * @return The name of the enum class
     */
    public static <E extends Enum<E>> String constructNetworkTableName(Class<E> pClass) {
        return pClass.getSimpleName().toUpperCase();
    }

    /**
     * Translates the NetworkTables into the Codex with the corresponding enumeration name
     */
    public void parseFromNetworkTables() {
        for(E e : EnumUtils.getEnums(mEnumClass)) {
            String key = e.name().toUpperCase();
            // System.out.println(kNetworkTable.getPath() + " " + key);
            Double value = kNetworkTable.getEntry(key).getDouble(Double.NaN);
            mCodex.set(e, value);
        }
    }
    /**
     * Translates and adds codex keys to a CSV Header in 'log' File path
     * @return returns the codex's header string
     */
    public String codexToCSVHeader() {
        return mCodex.getCSVHeader() + "TIME_RECEIVED";
    }
    /**
     * Translates and adds codex entries to a String
     * @return returns the codex's values string
     */
    public String codexToCSVLog() {
        return "\n"+mCodex.toCSV()+System.currentTimeMillis()/1000;
    }
    /**
     * Gives back a file with a path based on the enumeration name and date
     * The csv log file path (LOG_PATH_FORMAT) is .\logs\MM-dd-YYYY_HH-mm\EnumName.csv
     * @return File path of a certain enumeration
     */
    public File file() {
        // return new File(String.format(LOG_PATH_FORMAT, new SimpleDateFormat("MM-dd-YYYY_HH-mm").format(Calendar.getInstance().getTime()), csvIdentifier));
        String eventName = DriverStation.getInstance().getEventName();
        if ( eventName.length() <= 0 ) {
            eventName = "Default-Event";
        }
        return new File( String.format( LOG_PATH_FORMAT, 
                         eventName,
                         DriverStation.getInstance().getMatchType().name(),
                         DriverStation.getInstance().getMatchNumber() ) );
    }

    /**
     * 
     * @return Enumeration class associated to the codex of this CodexNetworkTablesParser instance
     */
    public Class<E> getEnum() {
        return mEnumClass;
    }
    public String getCSVIdentifier() {
        return csvIdentifier;
    }
}