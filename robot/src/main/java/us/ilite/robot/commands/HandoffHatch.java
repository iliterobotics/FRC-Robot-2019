package us.ilite.robot.commands;

import us.ilite.robot.modules.*;

public class HandoffHatch extends CommandQueue {

    public HandoffHatch(Elevator pElevator, Intake pIntake, HatchFlower pHatchFlower, CommandQueue pCommandQueue) {
        setCommands(
                new ParallelCommand(
                        new SetElevatorPosition(pElevator, EElevatorPosition.BOTTOM),
                        new SetHatchGrabberExtension(pHatchFlower, true)
                ),
                new SetIntakeState(pIntake, Intake.EWristPosition.HANDOFF),
                new GrabHatch(pHatchFlower),
                new SetElevatorPosition(pElevator, EElevatorPosition.HANDOFF_HEIGHT)
        );
    }

}
