package us.ilite.robot.hardware;
import us.ilite.robot.modules.EElevatorPosition;

public interface IElevatorHardware extends IHardware {

    void init();
    void zero();
    boolean checkHardware();
    void set(EElevatorPosition pDesiredPosition);
    
}