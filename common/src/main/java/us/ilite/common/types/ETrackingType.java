package us.ilite.common.types;

public enum ETrackingType {

    /*
    Target - 1
    Cargo - 3
    Hatch - 5
    Add one to prioritize right-hand targets.
    */
    TARGET_LEFT(1),
    TARGET_RIGHT(2),
    CARGO_LEFT(3),
    CARGO_RIGHT(4),
    HATCH_LEFT(5),
    HATCH_RIGHT(6);

    private final int kPipelineNum;

    private ETrackingType(int pPipelineNum) {
        kPipelineNum = pPipelineNum;
    }

    public int getPipeline() {
        return kPipelineNum;
    }

}