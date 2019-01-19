package us.ilite.common.io;

import java.util.List;

import com.flybotix.hfr.codex.Codex;
import com.flybotix.hfr.codex.CodexOf;
import com.flybotix.hfr.util.lang.EnumUtils;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;

public class CodexNetworkTablesParser<E extends Enum<E> & CodexOf<Double>> {

    private final NetworkTableInstance kNetworkTablesInstance = NetworkTableInstance.getDefault();
    private final NetworkTable kNetworkTable;

    private Codex<Double, E> mCodex;
    private Class<E> mEnumClass;

    public CodexNetworkTablesParser(Codex<Double, E> pCodex, Class<E> pEnumClass, String pNetworkTablesName) {
        mCodex = pCodex;
        mEnumClass = pEnumClass;
        kNetworkTable = kNetworkTablesInstance.getTable(constructNetworkTableName(pNetworkTablesName, pEnumClass));
    }

    public CodexNetworkTablesParser(Codex<Double, E> pCodex, Class<E> pEnumClass) {
        mCodex = pCodex;
        mEnumClass = pEnumClass;
        kNetworkTable = kNetworkTablesInstance.getTable(constructNetworkTableName(pEnumClass));
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

}