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
     * 
     * @param pCodex The codex that this CodexNetworkTablesParser instance is parsing
     * @param pEnumClass The enumeration that the codex uses
     * @param pNetworkTablesName Unique identifier when there are more than one codex instance of the same enum
     */
    public CodexNetworkTablesParser(Codex<Double, E> pCodex, Class<E> pEnumClass, String pNetworkTablesName) {
        mCodex = pCodex;
        mEnumClass = pEnumClass;
        kNetworkTable = kNetworkTablesInstance.getTable(constructNetworkTableName(pNetworkTablesName, pEnumClass));
        kLog = new File(String.format(LOG_PATH_FORMAT,
                                            new SimpleDateFormat("MM-dd-YYYY_HH-mm").format(Calendar.getInstance().getTime()),
                                            mEnumClass.getSimpleName()));
    }

    /**
     * 
     * @param pCodex The codex that this CodexNetworkTablesParser instance is parsing
     * @param pEnumClass The enumeration that the codex uses
     */
    public CodexNetworkTablesParser(Codex<Double, E> pCodex, Class<E> pEnumClass) {
        mCodex = pCodex;
        mEnumClass = pEnumClass;
        kNetworkTable = kNetworkTablesInstance.getTable(constructNetworkTableName(pEnumClass));
        kLog = new File(String.format(LOG_PATH_FORMAT,
                                            new SimpleDateFormat("MM-dd-YYYY_HH-mm").format(Calendar.getInstance().getTime()),
                                            mEnumClass.getSimpleName()));
    }

    /**
     * Translates the NetworkTables into its corresponding Codex
     */
    public void parseFromNetworkTables() {
        for(E e : EnumUtils.getEnums(mEnumClass)) {
            String key = e.name().toUpperCase();
            System.out.println(kNetworkTable.getPath() + " " + key);
            Double value = kNetworkTable.getEntry(key).getDouble(Double.NaN);
            mCodex.set(e, value);
        }
    }

    /**
     * Gets the NetworkTable name of the codex/enum class in question
     * when there are multiple instances of the same enumeration class
     */
    public static <E extends Enum<E>> String constructNetworkTableName(String pName, Class<E> pClass) {
        return pName.toUpperCase() + "-" + constructNetworkTableName(pClass);
    }

    /**
     * Gets the NetworkTable name of the codex/enum class in question
     * @param pClass the codex's enum class
     * @return the name of the enum class
     */
    public static <E extends Enum<E>> String constructNetworkTableName(Class<E> pClass) {
        return pClass.getSimpleName().toUpperCase();
    }

    /**
     * Translates and adds codex keys to a CSV Header in the LOG_PATH_FORMAT path
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
     * Translates and adds codex entries to CSV in `log` var File
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