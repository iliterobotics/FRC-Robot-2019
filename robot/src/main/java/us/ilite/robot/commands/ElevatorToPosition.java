package us.ilite.robot.commands;

import us.ilite.robot.modules.EElevatorPosition;
import us.ilite.robot.modules.Elevator;

public class ElevatorToPosition extends ACommand {

    private final Elevator mElevator;

    private final EElevatorPosition mDesiredPosition;

    public ElevatorToPosition(Elevator pElevator, EElevatorPosition pDesiredPosition) {
        mElevator = pElevator;
        mDesiredPosition = pDesiredPosition;
    }

    @Override
    public void init(double pNow) {

        mElevator.setDesiredPosition(mDesiredPosition);

    }

    @Override
    public boolean update(double pNow) {
        return mElevator.isAtPosition(mDesiredPosition);
    }

    @Override
    public void shutdown(double pNow) {

    }

}
