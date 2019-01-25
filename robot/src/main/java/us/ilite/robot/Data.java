package us.ilite.robot;

import com.flybotix.hfr.codex.Codex;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import us.ilite.common.types.drive.EDriveData;
import us.ilite.common.types.input.EDriverInputMode;
import us.ilite.common.types.input.ELogitech310;
import us.ilite.common.types.sensor.EGyro;
import us.ilite.lib.util.SimpleNetworkTable;

public class Data {

    public static final Supplier<SimpleNetworkTable> kLoggingTable;
    public static final Supplier<SimpleNetworkTable> kSmartDashboard;
    public static final Supplier<NetworkTable> kLimelight;
    public static final Supplier<SimpleNetworkTable> kDriverControlSelection;
    static {
        kLoggingTable =  Suppliers.memoize(new Supplier<SimpleNetworkTable>() {
            @Override
            public SimpleNetworkTable get() {
                return new SimpleNetworkTable("LoggingTable");
            }
        });
        kSmartDashboard =  Suppliers.memoize(new Supplier<SimpleNetworkTable>() {
            @Override
            public SimpleNetworkTable get() {
                return new SimpleNetworkTable("SmartDashboard");
            }
        });
        kDriverControlSelection = new Supplier<SimpleNetworkTable>(){
        
            @Override
            public SimpleNetworkTable get() {
                return new SimpleNetworkTable("DriverControlSelection") {
                    @Override
                    public void initKeys() {
                        getInstance().getEntry(EDriverInputMode.class.getSimpleName()).setDefaultNumber(-1);
                    }
                };
            }
        };
        kLimelight = new Supplier<NetworkTable>(){
        
            @Override
            public NetworkTable get() {
                return  NetworkTableInstance.getDefault().getTable("limelight");
            }
        };
    }

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
