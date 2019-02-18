package us.ilite.robot.commands;

import us.ilite.robot.modules.*;

public class GroundIntakeCargo extends CommandQueue {

    public GroundIntakeCargo(Elevator pElevator, Intake pIntake, CargoSpit pCargoSpit, HatchFlower pHatchFlower) {
        setCommands(
                new ParallelCommand(
                        new SetElevatorPosition(pElevator, EElevatorPosition.HATCH_BOTTOM),
                        new SetHatchGrabberExtension(pHatchFlower, HatchFlower.ExtensionState.UP),
                        new ReleaseHatch(pHatchFlower)
                ),
                new ParallelCommand(
                        new SetIntakeState(pIntake, Intake.EIntakeState.GROUND_CARGO),
                        new IntakeCargo(pCargoSpit)
                )
        );
    }

}
