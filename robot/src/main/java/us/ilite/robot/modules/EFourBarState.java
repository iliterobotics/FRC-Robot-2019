package us.ilite.robot.modules;

import static org.mockito.Mockito.RETURNS_MOCKS;

public enum EFourBarState {

    // TO-DO: get angles
    NORMAL( 0.0, 0.0 ),
    STOP( 0.0, 0.0 ),
    ACCELERATE( 0.0, 0.0 ),
    CRUISE_1( 0.0, 0.0 ),
    CRUISE_2( 0.0, 0.0 ),
    DECELERATE( 0.0, 0.0 ),
    LANDED( 0.0, 0.0 );

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
                                   EFourBarState.CRUISE_1,
                                   EFourBarState.CRUISE_2,
                                   EFourBarState.DECELERATE,
                                   EFourBarState.LANDED };
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