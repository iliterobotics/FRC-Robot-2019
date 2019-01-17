package us.ilite.robot.modules;

public enum EElevatorPosition {

    //TODO change vars
    BOTTOM(0d,0),
    MIDDLE(0d,0),
    TOP(0d,0);

    public double inches;
    public double mEncoderThreshold;
    public double mSetPointPower;

    //TODO implement PID

    EElevatorPosition( double pPower, int pEncoderThreshold  ) {
        this.mSetPointPower = pPower;
        this.mEncoderThreshold = pEncoderThreshold;

    }

    public double getSetPointPower() {
        return mSetPointPower;
    }

    public double mEncoderThreshold() {
        return mEncoderThreshold;
    }
    
}