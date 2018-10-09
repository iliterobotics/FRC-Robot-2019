package us.ilite.robot.modules;

import com.flybotix.hfr.codex.Codex;
import us.ilite.common.types.drive.EDriveData;
import us.ilite.common.types.sensor.EGyro;
import us.ilite.robot.Data;
import us.ilite.robot.hardware.DriveHardware;

/**
 * Class for running all mDriveData train control operations from both autonomous and
 * driver-control
 */
public class Drive extends Module {

    private Codex<Double, EGyro> mImuData;
    private Codex<Double, EDriveData> mDriveData;
    private DriveHardware mDriveHardware = new DriveHardware();

    public Drive(Data data) {
        this.mImuData = data.imu;
        this.mDriveData = data.drive;
    }


    @Override
    public void powerOnInit(double pNow) {
        mDriveHardware.init();
        mDriveHardware.zero();
    }

    @Override
    public void modeInit(double pNow) {

    }

    @Override
    public void update(double pNow) {


    }

    @Override
    public void shutdown(double pNow) {

    }

    @Override
    public void checkModule(double pNow) {

    }

}











