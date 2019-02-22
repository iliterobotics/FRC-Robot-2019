package us.ilite.robot.commands;

import us.ilite.robot.modules.HatchFlower;

public class ReleaseHatch extends FunctionalCommand {

    public ReleaseHatch(HatchFlower pHatchFlower) {
        super(pHatchFlower::pushHatch, () -> pHatchFlower.getGrabberState() == HatchFlower.GrabberState.RELEASE);
    }

}
