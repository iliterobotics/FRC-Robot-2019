package us.ilite.robot.hardware;

import edu.wpi.first.wpilibj.Solenoid;

public class SolenoidWrapper {
    private Boolean mCurrentState;
    private Solenoid mSolenoid;

    public SolenoidWrapper(Solenoid pSolenoid) {
        mSolenoid = pSolenoid;
        mCurrentState = mSolenoid.get();
    }

    public void set(Boolean desiredState) {
        if (desiredState != mCurrentState) {
            mSolenoid.set(desiredState);
            mCurrentState = desiredState;
        }
    }
}