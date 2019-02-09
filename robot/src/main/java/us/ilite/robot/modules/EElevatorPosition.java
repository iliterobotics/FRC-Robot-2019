package us.ilite.robot.modules;

public enum EElevatorPosition {

    //TODO find encoder threshold
    BOTTOM(0d,0),
    MIDDLE(0d,0),
    TOP(0d,0);

    private final double kEncoderThreshold;
    private final double kSetPointPower;

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