package us.ilite.robot.commands;

import us.ilite.robot.modules.HatchFlower;

public class GrabHatch extends FunctionalCommand {

    public GrabHatch(HatchFlower pHatchFlower) {
        super(pHatchFlower::captureHatch, () -> pHatchFlower.getHatchFlowerState() == HatchFlower.HatchFlowerState.CAPTURE);
    }

}
