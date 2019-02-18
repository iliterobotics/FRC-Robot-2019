package us.ilite.robot.commands;

import us.ilite.robot.modules.EElevatorPosition;
import us.ilite.robot.modules.Elevator;

public class SetElevatorPosition implements ICommand {

    private final Elevator mElevator;

    private final EElevatorPosition mDesiredPosition;

    public SetElevatorPosition(Elevator pElevator, EElevatorPosition pDesiredPosition) {
        mElevator = pElevator;
        mDesiredPosition = pDesiredPosition;
    }

    @Override
    public void init(double pNow) {

        mElevator.setDesirecPosition(mDesiredPosition);

    }

    @Override
    public boolean update(double pNow) {
        return mElevator.isAtPosition(mDesiredPosition);
    }

    @Override
    public void shutdown(double pNow) {

    }

}
