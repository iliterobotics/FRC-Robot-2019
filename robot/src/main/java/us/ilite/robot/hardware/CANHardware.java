package us.ilite.robot.hardware;


public abstract class CANHardware implements IHardware {

    // Hold CAN Related parameters
    private int miCanAddress;

    CANHardware(int canAddress) {
        this.miCanAddress = canAddress;
    }

    /**
     * @return the iCanAddress
     */
    public int getCanAddress() {
        return miCanAddress;
    }

}
