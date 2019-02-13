package us.ilite.robot.modules;

public enum EElevatorPosition {

    //TODO find encoder threshold
    BOTTOM(0d,0),
    MIDDLE(0d,0),
    TOP(0d,0),
    HANDOFF_HEIGHT(0d, 0);

    public final double kEncoderThreshold;
    public final double kSetPointPower;

    EElevatorPosition( double pPower, int pEncoderThreshold ) {
        this.kSetPointPower = pPower;
        this.kEncoderThreshold = pEncoderThreshold;

    }

    public double getSetPointPower() {
        return kSetPointPower;
    }

    public double mEncoderThreshold() {
        return kEncoderThreshold;
    }
    
}