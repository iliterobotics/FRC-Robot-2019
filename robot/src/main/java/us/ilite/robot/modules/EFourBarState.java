package us.ilite.robot.modules;

public enum EFourBarState {

    // TO-DO: get angles
    NORMAL( 0.0, 0.0 ),
    STOP( 0.0, 0.0 ),
    HOLD( 0.0, 0.0 ),
    ACCELERATE( 0.0, 0.0 ),
    DECELERATE( 0.0, 0.0 );

    private double mLowerAngularBound;
    private double mUpperAngularBound;
    EFourBarState( double pLowerAngularBound, double pUpperAngularBound ) {
        mLowerAngularBound = pLowerAngularBound;
        mUpperAngularBound = pUpperAngularBound;
    }
    
    public static EFourBarState fromDegrees( double currentDegrees ) {
        EFourBarState[] states = { EFourBarState.NORMAL,
                                   EFourBarState.STOP,
                                   EFourBarState.ACCELERATE,
                                   EFourBarState.DECELERATE };
        for ( EFourBarState state : states ) {
            if ( currentDegrees >= state.getLowerAngularBound() &&
                 currentDegrees <= state.getUpperAngularBound() ) {
                    return state;
            }
        }
        return null;
    }

    public double getLowerAngularBound() {
        return mLowerAngularBound;
    }

    public double getUpperAngularBound() {
        return mUpperAngularBound;
    }
}