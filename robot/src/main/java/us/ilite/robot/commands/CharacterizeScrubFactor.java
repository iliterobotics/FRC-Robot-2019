package us.ilite.robot.commands;

import com.flybotix.hfr.codex.Codex;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import us.ilite.common.Data;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.types.drive.EDriveData;
import us.ilite.robot.modules.Drive;
import us.ilite.robot.modules.DriveMessage;

/**
 * Calculates a "track scrub factor" based off of gyro readings and wheelbase by turning counter-clockwise a specified
 * amount of degrees.
 * UNTESTED
 * Thanks to Cameron Earle and Team 401 for their implementation.
 */
public class CharacterizeScrubFactor implements ICommand {

    private static final ILog sLog =Logger.createLog(CharacterizeScrubFactor.class);

    private final Drive mDrive;
    private final Data mData;

    private final double mTurnPower;
    private final double mTurnRadians;

    public CharacterizeScrubFactor(Drive pMDrive, Data pMData, double pMTurnPower, double pTurnDegrees) {
        mDrive = pMDrive;
        mData = pMData;
        mTurnPower = pMTurnPower;
        mTurnRadians = Math.toRadians(pTurnDegrees);
    }

    @Override
    public void init(double pNow) {
        // Zero gyro and encoders
        mDrive.zero();
    }

    @Override
    public boolean update(double pNow) {

        Codex<Double, EDriveData> driveData = mData.drive;
        double yaw = mDrive.getHeading().getRadians();

        mDrive.setDriveMessage(DriveMessage.getClampedTurnDrive(0.0, -mTurnPower));

        if(yaw >= mTurnRadians) {

            // Average distance traveled by wheels
            double arcLength = (driveData.get(EDriveData.LEFT_POS_INCHES) + Math.abs(driveData.get(EDriveData.RIGHT_POS_INCHES))) / 2.0;

            double calculatedWheelbase = arcLength / yaw * 2.0;

            sLog.error("Calculate Track Width: ", calculatedWheelbase);
            sLog.error("Calculated Track Scrub Factor: ", (SystemSettings.kDriveEffectiveWheelbase / calculatedWheelbase));

            mDrive.setDriveMessage(DriveMessage.kNeutral);

            return true;
        }

        return false;
    }

    @Override
    public void shutdown(double pNow) {

    }

}
