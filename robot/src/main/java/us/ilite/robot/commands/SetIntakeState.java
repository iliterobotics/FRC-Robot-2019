package us.ilite.robot.commands;

import us.ilite.robot.driverinput.DriverInput.EGamePiece;
import us.ilite.robot.modules.Intake;

public class SetIntakeState extends FunctionalCommand {

    public SetIntakeState(Intake pIntake, Intake.EIntakeState pDesiredState, EGamePiece pGamePieceType) {
        super(() ->{
            pIntake.setIntakeState(pDesiredState);
            pIntake.setGamePiece(pGamePieceType);
        } , () -> pIntake.hasReachedState(pDesiredState));
    }

}
