package us.ilite.robot;

import java.util.Collection;

import com.flybotix.hfr.codex.Codex;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import us.ilite.common.io.CodexNetworkTables;
import us.ilite.common.types.drive.EDriveData;
import us.ilite.common.types.sensor.EGyro;

public class Data {

    public CodexNetworkTables cod = CodexNetworkTables.getInstance();
    public Codex<Double, EGyro> imu = Codex.of.thisEnum(EGyro.class);
    public Codex<Double, EDriveData> drive = Codex.of.thisEnum(EDriveData.class);

    private NetworkTableInstance inst;
    private NetworkTableEntry gyroTest;
    private NetworkTable gyroTable;

    public Data simulated() {
        imu = Codex.of.thisEnum(EGyro.class);
        drive = Codex.of.thisEnum(EDriveData.class);
        
        registerCodices();
        sendCodices();
        
        inst = NetworkTableInstance.getDefault();
        gyroTable = inst.getTable("EGYRO");
        gyroTest = gyroTable.getEntry("ID");

       registerCodices();
       sendCodices();

       NetworkTableInstance inst = NetworkTableInstance.getDefault();
       NetworkTable gyro = inst.getTable("EGYRO");
       NetworkTableEntry gyroTest = gyro.getEntry("ID");
       System.out.println("*****              "+gyroTest.getNumber(0)+"              *****");
       return this;
    }


    public void registerCodices() {
        cod.registerCodex(EGyro.class);
        cod.registerCodex(EDriveData.class);
    }
    
    public void sendCodices() {
        cod.send(imu);
        cod.send(drive);
    }

    public void setGyroCodexTable(Double data) {
        imu.set(0, data);
    }
    public Number getgyroCodexTable() {
        return imu.get(0);
    }
    public Double getGyroCodexYaw() {
        return imu.get(0);
    }
    public Number getGyroNTHeading() {
        return gyroTest.getNumber(0);
    }
    
}
