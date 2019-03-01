package us.ilite.robot.hardware;
import us.ilite.robot.modules.Elevator;

public interface IElevatorHardware extends IHardware {

    void init();
    void zero();
    boolean checkHardware();
    void set(Elevator.EElevatorPosition pDesiredPosition);
    
}