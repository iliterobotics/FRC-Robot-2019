package us.ilite.display.simulation;

import com.flybotix.hfr.codex.Codex;
import com.team254.lib.geometry.Pose2d;
import com.team254.lib.geometry.Rotation2d;
import us.ilite.common.Data;
import us.ilite.common.types.drive.EDriveData;
import us.ilite.display.simulation.ui.FieldWindow;
import us.ilite.robot.modules.Drive;
import us.ilite.robot.modules.Module;

public class UiUpdater extends Module {

    private final Data mData;
    private final Drive mDrive;

    private final FieldWindow mWindow;

    public UiUpdater(Data mData, Drive pDrive, FieldWindow mWindow) {
        this.mData = mData;
        this.mDrive = pDrive;
        this.mWindow = mWindow;
    }

    @Override
    public void modeInit(double pNow) {

    }

    @Override
    public void periodicInput(double pNow) {

    }

    @Override
    public void update(double pNow) {
        Codex<Double, EDriveData> driveData = mData.drive;
        SimData latestData;

//        Pose2d currentPose = new Pose2d(
//                driveData.get(EDriveData.ODOM_X),
//                driveData.get(EDriveData.ODOM_Y),
//                Rotation2d.fromDegrees(driveData.get(EDriveData.ODOM_HEADING))
//        );
//        Pose2d targetPose = new Pose2d(
//                driveData.get(EDriveData.TARGET_X),
//                driveData.get(EDriveData.TARGET_Y),
//                Rotation2d.fromDegrees(driveData.get(EDriveData.TARGET_HEADING))
//        );
//
//        latestData = new SimData(currentPose, targetPose);

        latestData = new SimData(mDrive.getDriveController().getCurrentPose(), mDrive.getDriveController().getTargetPose());

        mWindow.update(pNow, latestData);
    }

    @Override
    public void shutdown(double pNow) {

    }

}
