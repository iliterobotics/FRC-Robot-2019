package us.ilite.robot.modules;

public enum EFourBarState {
    // TO-DO: get output
    
    NORMAL( 0 ),
    STOP( 0 ),
    ACCELERATE( 0 ),
    CRUISE_1( 0 ),
    CRUISE_2( 0 ),
    DECELLERATE( 0 ),
    LANDED( 0 );

    private double mOutput;
    EFourBarState( double pOutput ) {
        mOutput = pOutput;
    }

    public double getOutput() {
        return mOutput;
    }
}