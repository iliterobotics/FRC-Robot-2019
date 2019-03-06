package us.ilite.robot.modules;

public enum EElevatorPosition {

    //TODO find encoder threshold
    HATCH_BOTTOM(0),
    HATCH_MIDDLE(16),
    HATCH_TOP(34),
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