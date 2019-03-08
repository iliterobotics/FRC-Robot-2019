package us.ilite.common.types;

public enum ETrackingType {

    /*
    Target - 0
    Cargo - 2
    Line - 4
    Add one to prioritize right-hand targets.
    */
    TARGET_LEFT(0, -1, true),
    TARGET_RIGHT(1, 1, true),
    CARGO_LEFT(2, -1, false),
    CARGO_RIGHT(3, 1, false),
    LINE_LEFT(4, -1, false),
    LINE_RIGHT(5, 1, false),
    NONE(6, 0, false);

    private final int kPipelineNum;
    private final boolean kLedOn;
    private final int kTurnScalar;

    ETrackingType(int pPipelineNum, int pTurnScalar, boolean pLedOn) {
        kPipelineNum = pPipelineNum;
        kTurnScalar = pTurnScalar;
        kLedOn = pLedOn;
    }

    public int getPipeline() {
        return kPipelineNum;
    }

    public int getTurnScalar() {
        return kTurnScalar;
    }

    public boolean getLedOn() {
        return kLedOn;
    }

}