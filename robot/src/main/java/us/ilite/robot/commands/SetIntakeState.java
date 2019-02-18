package us.ilite.robot.commands;

import us.ilite.robot.modules.Intake;

public class SetIntakeState extends FunctionalCommand {

//    public SetIntakeState(Intake pIntake, Intake.EWristState pDesiredPosition) {
//        super(() -> pIntake.setWristState(pDesiredPosition), () -> pIntake.isAtPosition(pDesiredPosition));
//    }
    public SetIntakeState(Intake pIntake, Intake.EIntakeState pDesiredPosition) {
        super(() -> pIntake.setIntakeState( pDesiredPosition ), () -> pIntake.hasReachedState( pDesiredPosition ));
    }

}
