
package us.ilite.robot.modules;

import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.team254.lib.drivers.TalonSRXFactory;

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

//TODO elevator state and position

    EElevatorState mState = EElevatorState.STOP;
    EElevatorPosition mPosition = EElevatorPosition.BOTTOM;

    
    
    TalonSRX mMasterElevator, mFollowerElevator;
    

    public Elevator(Data pData) {
        
        mData = pData;



        //initialize motors
        mMasterElevator = TalonSRXFactory.createDefaultTalon(0);

    }

    
    public void shutdown(double pNow) {

    }
    
    public void modeInit(double pNow) {

    }
    
    public void periodicInput(double pNow) {

    }
    
    public void update( double pNow ) {

    }
}