package us.ilite.robot.modules;

public enum EFourBarState {

    // TO-DO: get angles for accelerate and decelerate
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
    
    /**
     * Gets a fourbar state based on the degrees of the robot
     * @param currentDegrees the current degrees of the bot
     * @return state represented by the inputted degrees
     */
    public static EFourBarState fromDegrees( double currentDegrees ) {
        EFourBarState[] states = { EFourBarState.ACCELERATE,
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