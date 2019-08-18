package us.ilite.robot.commands;

import us.ilite.robot.driverinput.DriverInput.EGamePiece;
import us.ilite.robot.modules.*;

public class GroundIntakeCargo extends CommandQueue {

    public GroundIntakeCargo(/**/ Intake pIntake, HatchFlower pHatchFlower) {
        setCommands(
                new ParallelCommand(
                        new SetElevatorPosition(ElevatorSingle.EElevatorPosition.HATCH_BOTTOM),
                        new SetHatchGrabberExtension(pHatchFlower, HatchFlower.ExtensionState.UP),
                        new ReleaseHatch(pHatchFlower)
                ),
                new ParallelCommand(
                        new SetIntakeState(pIntake, Intake.EIntakeState.GROUND, EGamePiece.CARGO),
                        new IntakeCargo()
                )
        );
    }

}
