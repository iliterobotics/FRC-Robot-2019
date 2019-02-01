package us.ilite.robot;

import com.flybotix.hfr.codex.Codex;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import us.ilite.common.LoggedData;
import us.ilite.common.io.CodexNetworkTables;
import us.ilite.common.types.drive.EDriveData;
import us.ilite.common.types.input.EDriverInputMode;
import us.ilite.common.types.input.ELogitech310;
import us.ilite.common.types.sensor.EGyro;
import us.ilite.common.lib.util.SimpleNetworkTable;

public class RobotData {

    public LoggedData loggedData = new LoggedData();

    public Codex<Double, EGyro> imu = loggedData.imu;
    public Codex<Double, EDriveData> drive = loggedData.drive;
    public Codex<Double, ELogitech310> driverinput = loggedData.driverinput;
    public Codex<Double, ELogitech310> operatorinput = loggedData.operatorinput;
    
}
