package us.ilite.robot.commands;

import us.ilite.robot.driverinput.DriverInput.EGamePiece;
import us.ilite.robot.modules.ElevatorSingle;
import us.ilite.robot.modules.HatchFlowerSingle;
import us.ilite.robot.modules.Intake;

public class GroundIntakeHatch extends CommandQueue {

    public GroundIntakeHatch(/**/ Intake pIntake) {
        setCommands(
                new ParallelCommand(
                        new SetElevatorPosition( ElevatorSingle.EElevatorPosition.HATCH_BOTTOM),
                        new ReleaseHatch(),
                        new SetHatchGrabberExtension( HatchFlowerSingle.ExtensionState.DOWN)
                ),
                new SetIntakeState(pIntake, Intake.EIntakeState.GROUND, EGamePiece.HATCH)
        );
    }
}
