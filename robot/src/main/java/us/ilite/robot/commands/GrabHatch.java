package us.ilite.robot.commands;

import us.ilite.robot.modules.HatchFlowerSingle;

public class GrabHatch extends FunctionalCommand {

    public GrabHatch() {
        super( HatchFlowerSingle.getInstance()::captureHatch, () -> HatchFlowerSingle.getInstance().getGrabberState() == HatchFlowerSingle.GrabberState.GRAB);
    }

}
