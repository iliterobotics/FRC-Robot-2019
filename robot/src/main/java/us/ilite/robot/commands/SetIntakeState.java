package us.ilite.robot.commands;

import us.ilite.robot.modules.Intake;

public class SetIntakeState extends ACommand {

    private final Intake mIntake;
    private final Intake.EWristPosition mDesiredPosition;

    public SetIntakeState(Intake pIntake, Intake.EWristPosition pDesiredPosition) {
        mIntake = pIntake;
        mDesiredPosition = pDesiredPosition;
    }

    @Override
    public void init(double pNow) {
        mIntake.setWrist(mDesiredPosition);
    }

    @Override
    public boolean update(double pNow) {
        return mIntake.isAtPosition(mDesiredPosition);
    }

    @Override
    public void shutdown(double pNow) {

    }

}
