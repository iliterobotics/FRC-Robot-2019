package us.ilite.robot.modules;

public enum EElevatorPosition {

    //TODO find encoder threshold
    HATCH_BOTTOM(1),
    HATCH_MIDDLE(18),
    HATCH_TOP(36),
    CARGO_BOTTOM(8.5),
    CARGO_MIDDLE(25),
    CARGO_TOP(41.5);

    private double kEncoderThreshold;

    EElevatorPosition( double pEncoderThreshold ) {
        this.kEncoderThreshold = pEncoderThreshold;

    }

    public double mEncoderThreshold() {
        return kEncoderThreshold;
    }


}