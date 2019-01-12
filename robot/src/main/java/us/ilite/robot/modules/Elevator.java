
package us.ilite.robot.modules;

import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import us.ilite.common.config.SystemSettings;
// import com.team254.lib.drivers.TalonSRXFactory;
import us.ilite.lib.drivers.TalonSRXFactory;

import edu.wpi.first.wpilibj.Talon;
import us.ilite.robot.Data;

// import com.ctre.pho/enix.motorcontrol.can.TalonSRX;

public class Elevator extends Module {

    private Data mData;

    private boolean mInitialized;
    private boolean mAtBottom = true;
    private boolean mAtTop = false;
    private int mCurrentEncoderTicks;
    private double mDesiredPower = 0;
    private boolean mAtDesiredPosition;
    private boolean mDesiredDirectionUp, mDesiredDirectionDown;

    EElevatorPosition mCurrentPosition = EElevatorPosition.BOTTOM;

    //TODO elevator state and position

    EElevatorState mState;
    EElevatorPosition mPosition;

    // public static final double TOP_LIMIT, BOTTOM_LIMIT, DEFAULT_LIMIT;

    TalonSRX mMasterElevator, mFollowerElevator;

    public Elevator(Data pData) {

        mData = pData;

        //initialize motors TODO make ids in systemsettings
        mMasterElevator = TalonSRXFactory.createDefaultTalon(0);
        mFollowerElevator = TalonSRXFactory.createDefaultTalon(0);

        mFollowerElevator.follow(mMasterElevator);

        //TODO Probably set inverted
        //TODO All the other talon things

        //initialization stuff
        mAtBottom = true;
        mAtTop = false;
        mDesiredDirectionDown = false;
        mDesiredDirectionUp = true;

        mPosition = EElevatorPosition.BOTTOM;
        mState = EElevatorState.STOP;

        mCurrentEncoderTicks = 0;

        //TODO maybe change the quad position
        mMasterElevator.getSensorCollection().setQuadraturePosition(0, 0);

    }

    public void shutdown(double pNow) {

    }

    public void modeInit(double pNow) {

    }

    public void periodicInput(double pNow) {

    }

    public void update(double pNow) {

        //TODO change selected sensor position
        mCurrentEncoderTicks = mMasterElevator.getSelectedSensorPosition(0);
        mDesiredDirectionUp = (mDesiredPower > 0);
        mDesiredDirectionDown = !mDesiredDirectionUp;

        //current limiting
        mAtTop = !mAtBottom && /*currentLimiting &&*/ mDesiredDirectionUp; //TODO could cause issues when it comes to holding position
        mAtBottom = !mAtBottom && /*currentLimiting && */ mDesiredDirectionDown; //TODO could cause issues duing hold position as well

        // if (curentLimited()) {
        //     mState = EElevatorState.STOP;
        // } 

    }

    //might just put into the rigular update method but who knows, really.
    public void updateElevator(double pNow) {

        if (Math.abs(mDesiredPower) > 0d && !mAtBottom /* and also if position buttons are not being used*/) {
            mState = EElevatorState.HOLD;
        } else {
            mState = EElevatorState.NORMAL;
        }

        if (mDesiredDirectionUp) {

        }

    }

    private void resetBottom() {
        mCurrentEncoderTicks = 0;
        zeroEncoder();
    }

    private void resetTop() {
        mMasterElevator.setSelectedSensorPosition(SystemSettings.kTopEncoderTicks, 0, SystemSettings.kCANTimeoutMs);
    }

    public void zeroEncoder() {
        mMasterElevator.setSelectedSensorPosition(0, 0, SystemSettings.kCANTimeoutMs);
    }

    private boolean shouldDecelerate( int pCurrentEncoderTicks, boolean pCurrentDirectionUp ) {
        // if()
    }

}