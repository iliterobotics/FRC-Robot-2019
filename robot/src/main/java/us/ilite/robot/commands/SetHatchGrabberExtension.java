package us.ilite.robot.commands;

import us.ilite.robot.modules.HatchFlowerSingle;

public class SetHatchGrabberExtension extends FunctionalCommand {

    public SetHatchGrabberExtension( HatchFlowerSingle.ExtensionState pExtensionState) {
        super(() -> HatchFlowerSingle.getInstance().setFlowerExtended(pExtensionState), () -> HatchFlowerSingle.getInstance().getExtensionState() == pExtensionState);
    }

}
