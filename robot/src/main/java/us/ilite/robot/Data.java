package us.ilite.robot;

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

    public Data simulated() {
       imu = Codex.of.thisEnum(EGyro.class);
       drive = Codex.of.thisEnum(EDriveData.class);

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

}
