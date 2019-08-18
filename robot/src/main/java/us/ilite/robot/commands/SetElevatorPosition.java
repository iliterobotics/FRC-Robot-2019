package us.ilite.robot.commands;

import us.ilite.robot.modules.ElevatorSingle;

public class SetElevatorPosition implements ICommand {

//    private final Elevator mElevator;

    private final ElevatorSingle.EElevatorPosition mDesiredPosition;

    public SetElevatorPosition(/**/ ElevatorSingle.EElevatorPosition pDesiredPosition) {
        mDesiredPosition = pDesiredPosition;
    }

    @Override
    public void init(double pNow) {

        ElevatorSingle.getInstance().setDesiredPosition(mDesiredPosition);

    }

    @Override
    public boolean update(double pNow) {
        return ElevatorSingle.getInstance().isAtPosition(mDesiredPosition);
    }

    @Override
    public void shutdown(double pNow) {

    }

}
