package us.ilite.common.types;

public enum ETrackingType {

    /*
    Target - 1
    Cargo - 3
    Hatch - 5
    Add one to prioritize right-hand targets.
    */
    TARGET_LEFT(1, -1),
    TARGET_RIGHT(2, 1),
    CARGO_LEFT(3, -1),
    CARGO_RIGHT(4, 1),
    LINE_LEFT(5, -1),
    LINE_RIGHT(6, 1);

    private final int kPipelineNum;
    private final int kTurnScalar;

    private ETrackingType(int pPipelineNum, int pTurnScalar) {
        kPipelineNum = pPipelineNum;
        kTurnScalar = pTurnScalar;
    }

    public int getPipeline() {
        return kPipelineNum;
    }

    public int getTurnScalar() {
        return kTurnScalar;
    }

}