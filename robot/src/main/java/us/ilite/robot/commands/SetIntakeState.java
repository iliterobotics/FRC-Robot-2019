package us.ilite.robot.commands;

import us.ilite.robot.modules.Intake;

public class SetIntakeState extends FunctionalCommand {

    public SetIntakeState(Intake pIntake, Intake.EIntakeState pDesiredState) {
        super(() -> pIntake.setIntakeState(pDesiredState), () -> pIntake.hasReachedState(pDesiredState));
    }

}
