package us.ilite.robot.modules;

public enum EElevatorPosition {

    //TODO find encoder threshold
    HATCH_BOTTOM(0.1d,500),
    HATCH_MIDDLE(0.1d,1000),
    HATCH_TOP(0.1d,1500),
    CARGO_BOTTOM(0.1d,500),
    CARGO_MIDDLE(0.1d,1000),
    CARGO_TOP(0.1d,1500);

    private double kEncoderThreshold;
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