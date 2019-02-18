package us.ilite.robot.commands;

import us.ilite.robot.modules.Intake;

public class SetIntakeState extends FunctionalCommand {

    public SetIntakeState(Intake pIntake, Intake.EWristPosition pDesiredPosition) {
        super(() -> pIntake.setWrist(pDesiredPosition), () -> pIntake.isAtPosition(pDesiredPosition));
    }

}
