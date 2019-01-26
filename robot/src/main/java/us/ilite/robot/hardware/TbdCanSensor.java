package us.ilite.robot.hardware;



public class TbdCanSensor extends CANHardware {

    public TbdCanSensor( int canAddress ) {
        super( canAddress );
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
