package us.ilite.robot.commands;

import us.ilite.robot.modules.EElevatorPosition;
import us.ilite.robot.modules.Elevator;
import us.ilite.robot.modules.HatchFlower;

public class LoadingStationIntakeHatch extends CommandQueue {

    public LoadingStationIntakeHatch(Elevator pElevator, HatchFlower pHatchFlower) {
        setCommands(
                new ParallelCommand(
                        new SetElevatorPosition(pElevator, EElevatorPosition.HATCH_BOTTOM),
                        new ReleaseHatch(pHatchFlower),
                        new SetHatchGrabberExtension(pHatchFlower, HatchFlower.ExtensionState.DOWN)
                ),
                new GrabHatch(pHatchFlower)
        );
    }

}
