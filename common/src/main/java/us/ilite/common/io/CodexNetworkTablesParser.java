package us.ilite.common.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import com.flybotix.hfr.codex.Codex;
import com.flybotix.hfr.codex.CodexOf;
import com.flybotix.hfr.util.lang.EnumUtils;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;

public class CodexNetworkTablesParser<E extends Enum<E> & CodexOf<Double>> {

    private static final String LOG_PATH_FORMAT = "./logs/%s/%s.csv"; //month day year hour min

    private final NetworkTableInstance kNetworkTablesInstance = NetworkTableInstance.getDefault();
    private final NetworkTable kNetworkTable;

    private Codex<Double, E> mCodex;
    private Class<E> mEnumClass;

    private File kLog;

    public CodexNetworkTablesParser(Codex<Double, E> pCodex, Class<E> pEnumClass, String pNetworkTablesName) {
        mCodex = pCodex;
        mEnumClass = pEnumClass;
        kNetworkTable = kNetworkTablesInstance.getTable(constructNetworkTableName(pNetworkTablesName, pEnumClass));
        kLog = new File(String.format(LOG_PATH_FORMAT,
                                            new SimpleDateFormat("MM-dd-YYYY_HH-mm").format(Calendar.getInstance().getTime()),
                                            mEnumClass.getSimpleName()));
    }

    public CodexNetworkTablesParser(Codex<Double, E> pCodex, Class<E> pEnumClass) {
        mCodex = pCodex;
        mEnumClass = pEnumClass;
        kNetworkTable = kNetworkTablesInstance.getTable(constructNetworkTableName(pEnumClass));
        kLog = new File(String.format(LOG_PATH_FORMAT,
                                            new SimpleDateFormat("MM-dd-YYYY_HH-mm").format(Calendar.getInstance().getTime()),
                                            mEnumClass.getSimpleName()));
    }

    public void parseFromNetworkTables() {
        List<E> enums = EnumUtils.getEnums(mEnumClass);
        for(E t : enums) {
            String key = t.name().toUpperCase();
            System.out.println(kNetworkTable.getPath() + " " + key);
            Double value = kNetworkTable.getEntry(key).getDouble(Double.NaN);
            mCodex.set(t, value);
        }
    }

    public static <E extends Enum<E>> String constructNetworkTableName(String pName, Class<E> pClass) {
        return pName + "-" + constructNetworkTableName(pClass);
    }

    public static <E extends Enum<E>> String constructNetworkTableName(Class<E> pClass) {
        return pClass.getSimpleName().toUpperCase();
    }

    /**
     * Translates and adds codex keys to a CSV Header in the LOG_PATH_FORMAT path
     */
    public void codexToCSVHeader() {
        try {
            handleCreation(kLog); //Makes the file
            Writer logger = new BufferedWriter(new FileWriter(kLog));
            String row = mCodex.getCSVHeader() + "TIME_SENT,TIME_RECEIVED";
            logger.append(row);
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
    public void codexToCSVLog(long robotTime) {
        try {
            Writer logger = new BufferedWriter(new FileWriter(kLog));
            String row = "\n"+mCodex.toCSV() + robotTime + "," + (System.currentTimeMillis()/1000);
            logger.append(row);
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
        if(pFile.getParentFile().exists()) pFile.getParentFile().mkdir();
        if(!pFile.exists()) pFile.createNewFile();
    }

}