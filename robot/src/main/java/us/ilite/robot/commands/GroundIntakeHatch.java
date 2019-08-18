package us.ilite.robot.commands;

import us.ilite.robot.driverinput.DriverInput.EGamePiece;
import us.ilite.robot.modules.ElevatorSingle;
import us.ilite.robot.modules.HatchFlower;
import us.ilite.robot.modules.Intake;

public class GroundIntakeHatch extends CommandQueue {

    public GroundIntakeHatch(/**/ Intake pIntake, HatchFlower pHatchFlower) {
        setCommands(
                new ParallelCommand(
                        new SetElevatorPosition( ElevatorSingle.EElevatorPosition.HATCH_BOTTOM),
                        new ReleaseHatch(pHatchFlower),
                        new SetHatchGrabberExtension(pHatchFlower, HatchFlower.ExtensionState.DOWN)
                ),
                new SetIntakeState(pIntake, Intake.EIntakeState.GROUND, EGamePiece.HATCH)
        );
    }
}
