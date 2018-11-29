package us.ilite.robot;

import com.flybotix.hfr.codex.Codex;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import us.ilite.common.types.drive.EDriveData;
import us.ilite.common.types.input.EDriverInputMode;
import us.ilite.common.types.input.ELogitech310;
import us.ilite.common.types.sensor.EGyro;
import us.ilite.lib.util.SimpleNetworkTable;

public class Data {

    public static SimpleNetworkTable LOGGING_TABLE = new SimpleNetworkTable("LOGGING_TABLE");
    public static SimpleNetworkTable SMART_DASHBOARD = new SimpleNetworkTable("SmartDashboard");
    public static NetworkTable LIMELIGHT = NetworkTableInstance.getDefault().getTable("limelight");
    public static SimpleNetworkTable DRIVER_CONTROL_TABLE = new SimpleNetworkTable("DRIVER_CONTROL_TABLE") {
        @Override
        public void initKeys() {
            getInstance().getEntry(EDriverInputMode.class.getSimpleName()).setDefaultNumber(-1);
        }
    };

    public Codex<Double, EGyro> imu = Codex.of.thisEnum(EGyro.class);
    public Codex<Double, EDriveData> drive = Codex.of.thisEnum(EDriveData.class);
    public Codex<Double, ELogitech310> driverinput = Codex.of.thisEnum(ELogitech310.class);
    public Codex<Double, ELogitech310> operatorinput = Codex.of.thisEnum(ELogitech310.class);

   public Data simulated() {
       imu = Codex.of.thisEnum(EGyro.class);
       drive = Codex.of.thisEnum(EDriveData.class);
       return this;
   }

}
