package us.ilite.robot.hardware;



public class BeamBreak extends CANHardware {

    public BeamBreak(int canAddress) {
        super(canAddress);
    }

    @Override
    public void init() {

    }

    @Override
    public void zero() {

    }

    @Override
    public boolean checkHardware() {
        return false;
    }
}
