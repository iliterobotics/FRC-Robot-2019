package us.ilite.common.types;

public enum ETrackingType {

    /*
    Target - 0
    Cargo - 2
    Line - 4
    Add one to prioritize right-hand targets.
    */
    TARGET_LEFT(0, -1),
    TARGET_RIGHT(1, 1),
    CARGO_LEFT(2, -1),
    CARGO_RIGHT(3, 1),
    LINE_LEFT(4, -1),
    LINE_RIGHT(5, 1);

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