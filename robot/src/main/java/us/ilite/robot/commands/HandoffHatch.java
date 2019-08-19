package us.ilite.robot.commands;

import us.ilite.robot.driverinput.DriverInput.EGamePiece;
import us.ilite.robot.modules.*;

public class HandoffHatch extends CommandQueue {

    public HandoffHatch(/**/ Intake pIntake) {
        setCommands(
                new ParallelCommand(
                        new SetElevatorPosition(ElevatorSingle.EElevatorPosition.HATCH_BOTTOM),
                        new SetHatchGrabberExtension(HatchFlowerSingle.ExtensionState.DOWN)
                ),
                new SetIntakeState(pIntake, Intake.EIntakeState.HANDOFF, EGamePiece.HATCH),
                new GrabHatch(),
                new SetHatchGrabberExtension(HatchFlowerSingle.ExtensionState.DOWN)
        );
    }

}
