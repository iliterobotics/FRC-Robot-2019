package us.ilite.robot.commands;

import us.ilite.robot.modules.HatchFlowerSingle;

public class ReleaseHatch extends FunctionalCommand {

    public ReleaseHatch() {
        super( HatchFlowerSingle.getInstance()::pushHatch, () -> HatchFlowerSingle.getInstance().getGrabberState() == HatchFlowerSingle.GrabberState.RELEASE);
    }

}
