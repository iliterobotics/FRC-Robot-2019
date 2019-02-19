package us.ilite.robot.commands;

import us.ilite.robot.modules.HatchFlower;

public class SetHatchGrabberExtension extends FunctionalCommand {

    public SetHatchGrabberExtension(HatchFlower pHatchFlower, HatchFlower.ExtensionState pExtensionState) {
        super(() -> pHatchFlower.setFlowerExtended(pExtensionState), () -> pHatchFlower.getExtensionState() == pExtensionState);
    }

}
