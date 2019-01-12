package us.ilite.robot.modules;

public enum EElevatorState {

    NORMAL(0),
    STOP(0),
    HOLD(0),
    DECEL_TOP(0),
    DECEL_BOTTOM(0);

    double mPower;

    EElevatorState(double pPower) {
        this.mPower = pPower;
    }
    
    double getPower() {
        return mPower;
    }

}