package us.ilite.robot.commands;

import us.ilite.robot.modules.EElevatorPosition;
import us.ilite.robot.modules.Elevator;
import us.ilite.robot.modules.HatchFlower;
import us.ilite.robot.modules.Intake;

import java.util.Arrays;
import java.util.List;

public class Handoff extends ACommand {

    private Elevator mElevator;
    private Intake mIntake;
    private HatchFlower mHatchFlower;

    private CommandQueue mCommandQueue = new CommandQueue();

    private final EHandoffType mHandoffType;

    public Handoff(Elevator pElevator, Intake pIntake, HatchFlower pHatchFlower, EHandoffType pHandoffType) {
        mElevator = pElevator;
        mIntake = pIntake;
        mHatchFlower = pHatchFlower;
        mHandoffType = pHandoffType;
    }

    public enum EHandoffType {
        HATCH,
        CARGO
    }


    @Override
    public void init(double pNow) {

        ACommand[] mHandoffSequence = new ACommand[]{
                new ParallelCommand(
                        new ElevatorToPosition(mElevator, EElevatorPosition.BOTTOM),
                        new SetHatchGrabberPosition(mHatchFlower, true)
                ),
                new SetIntakeState(mIntake, Intake.EWristPosition.HANDOFF),
                getHandoffCommand(mHandoffType)
        };

        mCommandQueue.setCommands(mHandoffSequence);
        mCommandQueue.init(pNow);
    }

    @Override
    public boolean update(double pNow) {
        mCommandQueue.update(pNow);
        return mCommandQueue.isDone();
    }

    @Override
    public void shutdown(double pNow) {
        mCommandQueue.shutdown(pNow);
    }

    private ACommand getHandoffCommand(EHandoffType pHandoffType) {
        switch(pHandoffType) {
            case HATCH:
                return new InstantCommand(mHatchFlower::captureHatch);
            case CARGO:
            default:
                return new EmptyCommand();
        }
    }

}
