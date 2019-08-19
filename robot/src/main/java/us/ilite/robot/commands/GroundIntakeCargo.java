package us.ilite.robot.commands;

import us.ilite.robot.driverinput.DriverInput.EGamePiece;
import us.ilite.robot.modules.*;

public class GroundIntakeCargo extends CommandQueue {

    public GroundIntakeCargo(/**/ Intake pIntake) {
        setCommands(
                new ParallelCommand(
                        new SetElevatorPosition(ElevatorSingle.EElevatorPosition.HATCH_BOTTOM),
                        new SetHatchGrabberExtension(HatchFlowerSingle.ExtensionState.UP),
                        new ReleaseHatch()
                ),
                new ParallelCommand(
                        new SetIntakeState(pIntake, Intake.EIntakeState.GROUND, EGamePiece.CARGO),
                        new IntakeCargo()
                )
        );
    }

}
