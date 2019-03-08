package us.ilite.robot.hardware;

/**
 * Keeps track of an encoder's position, velocity, and acceleration based on
 * velocity
 */
public class SimTalonEncoder {

    private double mPosition, mVelocity, mAccel;

    public void zero() {
        mPosition = mVelocity = mAccel = 0.0;
    }

    public void update(double pDt, double pVelocity) {
        // Convert to units of per 100ms
        double dt = pDt * 10.0;

        mAccel = (pVelocity - mVelocity) / dt;
        mVelocity = pVelocity;
        mPosition += (mVelocity * dt) /*+ (0.5 * mAccel * dt * dt)*/;
    }

    public double getPosition() {
        return mPosition;
    }

    public double getVelocity() {
        return mVelocity;
    }

    public double getAccel() {
        return mAccel;
    }

}
