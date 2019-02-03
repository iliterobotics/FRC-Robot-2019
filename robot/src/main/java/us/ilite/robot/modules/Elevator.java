
package us.ilite.robot.modules;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMax.IdleMode;
import com.team254.lib.util.Units;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.team254.lib.drivers.talon.TalonSRXFactory;

import us.ilite.common.config.SystemSettings;
import us.ilite.lib.drivers.SparkMaxFactory;

import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Timer;
import us.ilite.common.lib.control.PIDController;
import us.ilite.common.lib.control.PIDGains;
import com.team254.lib.util.Util;
import us.ilite.common.Data;


public class Elevator extends Module {

    private Data mData;
    private boolean mAtBottom = true;
    private boolean mAtTop = false;
    private int mCurrentEncoderTicks;
    private double mDesiredPower = 0;
    private boolean mAtDesiredPosition;
    private boolean mDesiredDirectionUp, mDesiredDirectionDown;
    private boolean mDesiredPositionAboveInitial = false;
    private double mMaxPower = 1.0d;
    private double mMinPower = -1.0d;
    private double mHoldVoltage = 0.5; //NOT final
    private boolean mSettingPosition = false;
    private int kCansparkId = 15; //TODO change ID
    private int kTalonId = 15; //TODO change ID

    //TODO need to figure out these encoder thresholds
    private int mDownEncoderThreshold = SystemSettings.kLowerElevatorEncoderThreshold;
    private int mUpEncoderThreshold = SystemSettings.kLowerElevatorEncoderThreshold;

    EElevatorPosition mCurrentPosition = EElevatorPosition.BOTTOM;

    EElevatorState mCurrentState = EElevatorState.NORMAL;
    EElevatorPosition mDesiredPosition;

    CANSparkMax mMasterElevator;
    TalonSRX mEncoder = TalonSRXFactory.createDefaultTalon(kTalonId);

    public Elevator(Data pData) {

        this.mData = pData;

        //Create default NEO and set the ramp rate
        mMasterElevator = SparkMaxFactory.createDefaultSparkMax(kCansparkId, MotorType.kBrushless);
        mMasterElevator.setIdleMode(IdleMode.kBrake);
        mMasterElevator.setRampRate(SystemSettings.kELevatorControlLoopPeriod);

        //We start at the bottom
        mAtBottom = true;
        mAtTop = false;
        mDesiredDirectionUp = true;

        //Make sure the elevator is stopped upon initialization
        mDesiredPosition = EElevatorPosition.BOTTOM;
        mCurrentState = EElevatorState.STOP;

        mCurrentEncoderTicks = 0;
    }

    public void shutdown(double pNow) {

    }

    public void modeInit(double pNow) {

    }

    public void periodicInput(double pNow) {

    }

    public void update(double pNow) {

        //Get encoder value from secondary Talon
        mCurrentEncoderTicks = getEnocderPosition();
        mDesiredDirectionUp = (mDesiredPower > 0);

        updateElevator(pNow);
        mDesiredPower = calculateDesiredPower(mCurrentState);

        mMasterElevator.set(Util.limit(mDesiredPower, mMinPower, mMaxPower));

    }

    public void updateElevator(double pNow) {

        //TODO test this
        //Should override any automated control
        if (Math.abs(mDesiredPower) ==  0d && !mAtBottom) { //Hold if input is absent and we aren't at the bottom
            mCurrentState = EElevatorState.HOLD;
        } else {
            mCurrentState = EElevatorState.NORMAL;
        }

        if (mDesiredDirectionUp) {
            shouldDecelerate(mCurrentEncoderTicks, mDesiredDirectionUp);
        }

    }

    private void resetBottom() {
        mCurrentEncoderTicks = 0;
        zeroEncoder();
    }

    private void resetTop() {
        setEncoderPosition(0); //Find out top position encoder ticks
    }

    public void zeroEncoder() {
        setEncoderPosition(0); //Actually should be zero
    }

    //Checks if we are close enough to the top or bottom 
    //that we should be decelerating
    private void shouldDecelerate(int pCurrentEncoderTicks, boolean pCurrentDirectionUp) {
        if (pCurrentDirectionUp) {
            if (pCurrentEncoderTicks >= mUpEncoderThreshold) {
                mCurrentState = EElevatorState.DECEL_TOP;
            }
        } else if (!pCurrentDirectionUp) {
            if (pCurrentEncoderTicks >= mDownEncoderThreshold) {
                mCurrentState = EElevatorState.DECEL_BOTTOM;
            }
        }
    }
    
    public void toBottom() {
        mDesiredPosition = EElevatorPosition.BOTTOM;
        mCurrentState = EElevatorState.SET_POSITION;
    }

    public void toTop() {
        mDesiredPosition = EElevatorPosition.BOTTOM;
        mCurrentState = EElevatorState.SET_POSITION;
    }


    private double setPosition(EElevatorPosition pDesiredPosition, double pCurrentTime) {
    

        double power;
        //If the current encoder ticks is less than the threshold of the desired position, then
        //The desired position is above the initial position
        mDesiredPositionAboveInitial = (mCurrentEncoderTicks < pDesiredPosition.kEncoderThreshold);
        
        

        //If the we are under the desired position and we are not in the encoder position for said
        //position, then we are still setting position
        if (mDesiredPositionAboveInitial && mCurrentEncoderTicks >= pDesiredPosition.kEncoderThreshold) {
            mSettingPosition = false;
        } else if(!mDesiredPositionAboveInitial && mCurrentEncoderTicks <= pDesiredPosition.kEncoderThreshold) { //if we are above it, same logic
            mSettingPosition = false;
        } else {
            mCurrentPosition = pDesiredPosition;
            mSettingPosition = true;
        }

        //If we make it to or beyond our position, then hold.
        if (!mSettingPosition) {
            power = EElevatorState.HOLD.getPower();
        } else {
            PIDController pidController = new PIDController(SystemSettings.kElevatorGains,
                    SystemSettings.kELevatorControlLoopPeriod);
            pidController.setSetpoint(pDesiredPosition.kEncoderThreshold); //Our set point is the threshold of the destination state
            power = pidController.calculate(mCurrentEncoderTicks, pCurrentTime);
            power = Util.limit(mDesiredPower, mMinPower, mMaxPower); //limit power from -1.0 - 1.0
        }

        return power;
 
    }

    public int getCurrentEncoderTicks() {
        return mCurrentEncoderTicks;
    }

    public void setPower( int pPower ) {
        mDesiredPower = pPower;
    }

    public boolean getDirection() {
        return mDesiredDirectionUp;
    }

    public double getDesiredPower() {
        return mDesiredPower;
    }


    //Getting voltage and current
    public double getBusVoltage() {
        return mMasterElevator.getBusVoltage();
    }

    public double getMasterCurrent() {
        return mMasterElevator.getOutputCurrent();
    }

    public boolean finishedPositioning() {
        return !mSettingPosition;
    }

    public int getEnocderPosition() {
        return mEncoder.getSelectedSensorPosition(0);
    }

    //Need to find a way to set the encoder position
    public void setEncoderPosition(int pTicks) {
        mEncoder.setSelectedSensorPosition(0);
    }


    private double calculateDesiredPower(EElevatorState pCurrentState) {

        double power = 0d;

        switch (pCurrentState) {
        case NORMAL:
            power = mDesiredPower; //10% of the actual power equals what the driver wants
            mSettingPosition = false;
            break;
        case DECEL_TOP:
            //TODO test decelerating
            power = Math.min(mDesiredPower, pCurrentState.getPower()); //Use the lower value to ease into the top
            break;
        case DECEL_BOTTOM:
            power = Math.max(mDesiredPower, pCurrentState.getPower()); //Use the higher value to ease into the bottom
            break;
        case HOLD:
            power = mHoldVoltage / getBusVoltage(); //Need to test
            break;
        case STOP:
            power = EElevatorState.STOP.getPower(); //Essentially zero, but we don't like magic numbers
            break;
        case SET_POSITION:
            power = setPosition(mDesiredPosition, Timer.getFPGATimestamp()); //Start setting position with the current time
            break;
        default:
            System.out.println("Somehow reached an unaccounted state with " + pCurrentState.toString()); //In case, somehow, we reach this
        }

        return Util.limit(power, mMinPower, mMaxPower);

    }

    //This method is made to be called from outside the method
    //the state should only reach set point when driver input call it
    public void setStatePosition(EElevatorPosition pToPosition) {
        mCurrentState = EElevatorState.SET_POSITION;
        mDesiredPosition = pToPosition;
        mSettingPosition = true; //Keeps track that we are in the process of setting the position
    }

    public double getCurrent() {
        return mMasterElevator.getOutputCurrent();
    }


}