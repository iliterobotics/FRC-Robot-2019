package us.ilite.common.types;

public enum ETrackingType {

    /*
    Target - 1
    Cargo - 3
    Hatch - 5
    Add one to prioritize right-hand targets.
    */
    TARGET_TRACK(1),
    CARGO_TRACK(3),
    HATCH_TRACK(5);

    private final int kLeftPipelineNum;

    private ETrackingType(int pPipelineNum) {
        kLeftPipelineNum = pPipelineNum;
    }

    public int getLeftPipelineNum() {
        return kLeftPipelineNum;
    }

    public int getRightPipelineNum() {
        return kLeftPipelineNum + 1;
    }

}