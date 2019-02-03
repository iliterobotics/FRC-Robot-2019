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

    public double getLowerAngularBound() {
        return mLowerAngularBound;
    }

    public double getUpperAngularBound() {
        return mUpperAngularBound;
    }
}