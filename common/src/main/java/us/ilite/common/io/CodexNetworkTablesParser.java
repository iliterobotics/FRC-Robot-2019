package us.ilite.common.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.flybotix.hfr.codex.Codex;
import com.flybotix.hfr.codex.CodexOf;
import com.flybotix.hfr.util.lang.EnumUtils;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;

public class CodexNetworkTablesParser<E extends Enum<E> & CodexOf<Double>> {

    private static final String LOG_PATH_FORMAT = System.getProperty("user.dir")+"/logs/%s/%s.csv"; //month day year hour min

    private final NetworkTableInstance kNetworkTablesInstance = NetworkTableInstance.getDefault();
    private final NetworkTable kNetworkTable;

    private Codex<Double, E> mCodex;
    private Class<E> mEnumClass;

    private File kLog;

    /**
     * Use this constructor when you have multiple codices that have the same enumeration
     * @param pCodex The codex that this CodexNetworkTablesParser instance is parsing
     * @param pNetworkTablesName Composite key for the NetworkTable
     */
    public CodexNetworkTablesParser(Codex<Double, E> pCodex, String pNetworkTablesName) {
        mCodex = pCodex;
        mEnumClass = mCodex.meta().getEnum(); //This gets the enumeration that corresponds to the codex
        kNetworkTable = kNetworkTablesInstance.getTable(constructNetworkTableName(mEnumClass, pNetworkTablesName));
        kLog = new File(String.format(LOG_PATH_FORMAT,
                                            new SimpleDateFormat("MM-dd-YYYY_HH-mm").format(Calendar.getInstance().getTime()),
                                            mEnumClass.getSimpleName()));
    }

    /**
     * 
     * @param pCodex The codex that this CodexNetworkTablesParser instance is parsing
     */
    public CodexNetworkTablesParser(Codex<Double, E> pCodex) {
        mCodex = pCodex;
        mEnumClass = mCodex.meta().getEnum();
        kNetworkTable = kNetworkTablesInstance.getTable(constructNetworkTableName(mEnumClass));
        kLog = new File(String.format(LOG_PATH_FORMAT,
                                            new SimpleDateFormat("MM-dd-YYYY_HH-mm").format(Calendar.getInstance().getTime()),
                                            mEnumClass.getSimpleName()));
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
     */
    public void codexToCSVHeader() {
        try {
            handleCreation(kLog); //Makes the file if it doesnt exist
            Writer logger = new BufferedWriter(new FileWriter(kLog));
            String row = mCodex.getCSVHeader() + ",TIME_RECEIVED";
            logger.write(row);
            logger.close();
        }
        catch(IOException e) {
            System.err.printf("Error writing CSV header");
            e.printStackTrace();
        }
    }
    /**
     * Translates and adds codex entries to CSV in 'log' File path
     */
    public void codexToCSVLog() {
        try {
            Writer logger = new BufferedWriter(new FileWriter(kLog));
            String row = "\n"+mCodex.toCSV() + "," + (System.currentTimeMillis()/1000);
            logger.write(row);
            logger.close();
        }
        catch(IOException e) {
            System.err.printf("Error logging into CSV");
            e.printStackTrace();
        }
    }
    /**
     * Makes the log file if it doesn't already exist
     */
    private void handleCreation(File pFile) throws IOException {
        //Makes every folder before the file if the CSV's parent folder doesn't exist
        if(!pFile.getParentFile().exists()) pFile.getParentFile().mkdirs();

        //Creates the .CSV if it doesn't exist
        if(!pFile.exists()) pFile.createNewFile();
    }

}