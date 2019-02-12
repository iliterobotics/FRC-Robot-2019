package us.ilite.robot.modules;

public enum EElevatorPosition {

    BOTTOM(0.1d,500),
    MIDDLE(0.1d,1000),
    TOP(0.1d,1500);

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