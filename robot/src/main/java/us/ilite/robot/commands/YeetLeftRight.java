package us.ilite.robot.commands;

import us.ilite.robot.modules.Drive;
import us.ilite.robot.modules.DriveMessage;

public class YeetLeftRight implements ICommand {

    private Drive mDrive;

    public YeetLeftRight(Drive pDrive) {
        mDrive = pDrive;
    }

    @Override
    public void init(double pNow) {

    }

    @Override
    public boolean update(double pNow) {

        return false;
    }

    @Override
    public void shutdown(double pNow) {

    }
}
