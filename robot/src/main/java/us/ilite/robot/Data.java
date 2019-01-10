package us.ilite.robot;

import com.flybotix.hfr.codex.Codex;
import com.flybotix.hfr.io.CodexNetworkTables;

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
