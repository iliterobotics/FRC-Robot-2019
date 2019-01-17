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
        gyroTable = inst.getTable("EGYRO"); //gyro table
        gyroTest = gyroTable.getEntry("ID"); //gyro id

       registerCodices();
       sendCodices();

       return this;
    }


    public void registerCodices() { //registers/makes codex table with 
        cod.registerCodex(EGyro.class);
        cod.registerCodex(EDriveData.class);
    }
    
    public void sendCodices() { //sends codex tables to NT
        cod.send(imu);
        cod.send(drive);
    }

    public void setGyroCodexYaw(Double data) { //sets gyro yaw in codex table
        imu.set(0, data); //index 0 is yaw
    }
    public Double getGyroCodexYaw() { //gets gyro yaw in codex table
        return imu.get(0); //index 0 is yaw
    }
    public Number getGyroNTHeading() { //WIP
        return gyroTest.getNumber(0);
    }
    
}
