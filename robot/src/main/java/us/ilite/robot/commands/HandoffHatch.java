package us.ilite.robot.commands;

import us.ilite.robot.driverinput.DriverInput.EGamePiece;
import us.ilite.robot.modules.*;

public class HandoffHatch extends CommandQueue {

    public HandoffHatch(Elevator pElevator, Intake pIntake, HatchFlower pHatchFlower) {
        setCommands(
                new ParallelCommand(
                        new SetElevatorPosition(pElevator, Elevator.EElevatorPosition.HATCH_BOTTOM),
                        new SetHatchGrabberExtension(pHatchFlower, HatchFlower.ExtensionState.DOWN)
                ),
                new SetIntakeState(pIntake, Intake.EIntakeState.HANDOFF, EGamePiece.HATCH),
                new GrabHatch(pHatchFlower),
                new SetHatchGrabberExtension(pHatchFlower, HatchFlower.ExtensionState.DOWN)
        );
    }

}
