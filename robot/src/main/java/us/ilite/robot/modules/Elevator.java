
package us.ilite.robot.modules;

import com.revrobotics.CANPIDController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import com.revrobotics.ControlType;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.types.manipulator.EElevator;
import us.ilite.lib.drivers.SparkMaxFactory;

import edu.wpi.first.wpilibj.Encoder;
import us.ilite.common.lib.control.PIDController;
import com.team254.lib.util.Util;
import us.ilite.common.Data;


public class Elevator extends Module {

    private Data mData;
    private boolean mAtBottom = true;
    private boolean mAtTop = false;
    private double mCurrentEncoderTicks;
    private double mDesiredPower = 0;
    private boolean mAtDesiredPosition;
    private boolean mDesiredDirectionUp, mDesiredDirectionDown;
    private boolean mDesiredPositionAboveInitial = false;
    private double mMaxPower = 1.0d;
    private double mMinPower = -1.0d;
    private double mHoldVoltage = 0.5; // NOT final
    private boolean mSettingPosition = false;
    private int kCansparkId = 9; // TODO change ID (I think the actual robot's is 15)
    private PIDController mPidController;
    private double mCurrentTime;
    private double mNeoEncoderPosition;
    private CANPIDController mCanController;
    private int mSmartMotionSlot = 0; //TODO test
//    public Codex<Double, EElevator> elevatorCodex = Codex.of.thisEnum(EElevator.class);

    private double mBottomEncoderTicks = 0;
    private double mTopEncoderTicks = 0;

    EElevatorPosition mCurrentPosition = EElevatorPosition.BOTTOM;
    EElevatorState mCurrentState = EElevatorState.NORMAL;
    EElevatorPosition mDesiredPosition;
    EControlMode mCurrentControlMode = EControlMode.MOTION_MAGIC; //TODO test.

    CANSparkMax mMasterElevator;
    Encoder mEncoder;


    public Elevator(Data pData) {

        this.mData = pData;

        this.mPidController = new PIDController(SystemSettings.kElevatorPositionGains, 100, 1500, SystemSettings.kControlLoopPeriod);
        this.mEncoder = new Encoder(0, 1, false, Encoder.EncodingType.k4X);

        // Create default NEO
        mMasterElevator = SparkMaxFactory.createDefaultSparkMax(kCansparkId, MotorType.kBrushless);
        mMasterElevator.setIdleMode(IdleMode.kBrake);
        this.mCanController = mMasterElevator.getPIDController();

        // mMasterElevator.setRampRate(SystemSettings.kElevatorRampRate);
        mMasterElevator.setSmartCurrentLimit(SystemSettings.kElevatorSmartCurrentLimit);
        mMasterElevator.setSecondaryCurrentLimit(SystemSettings.kElevatorSecondaryCurrentLimit);

        //Setting PID Coefficients for Motion Magic
        mCanController.setP( SystemSettings.kElevatorMotionP );
        mCanController.setI( SystemSettings.kElevatorMotionI );
        mCanController.setD( SystemSettings.kElevatorMotionD );
        mCanController.setFF( SystemSettings.kElevatorMotionF );

        mCanController.setOutputRange( mMinPower, mMaxPower );
        mCanController.setSmartMotionMaxAccel( SystemSettings.kMaxElevatorAcceleration, mSmartMotionSlot );
        mCanController.setSmartMotionMaxVelocity( SystemSettings.kMaxElevatorVelocity, mSmartMotionSlot );
        mCanController.setSmartMotionAllowedClosedLoopError( SystemSettings.kElevatorAllowedError, mSmartMotionSlot );

        // We start at the bottom
        mAtBottom = true;
        mAtTop = false;
        mDesiredDirectionUp = true;

        // Make sure the elevator is stopped upon initialization
        mDesiredPosition = EElevatorPosition.BOTTOM;
        mCurrentState = EElevatorState.STOP;
        mCurrentEncoderTicks = 0;
    }

    public void shutdown(double pNow) {

    }

    public void modeInit(double pNow) {
        mMinPower = SystemSettings.kElevatorMinPower;
        mMaxPower = SystemSettings.kElevatorMaxPower;
        mCurrentEncoderTicks = 0;
        zeroEncoder();
        mCurrentTime = pNow;
        mPidController.reset();
    }

    public void periodicInput(double pNow) {
    }

    public void update(double pNow) {
        // System.out.println(mData.elevator);

        mCurrentTime = pNow;

        // Get encoder value from secondary Talon
        mCurrentEncoderTicks = getEncoderPosition();
        mNeoEncoderPosition = mMasterElevator.getEncoder().getPosition();
        mDesiredDirectionUp = (mDesiredPower > 0);

//        updateElevatorState(pNow);
        double output  = calculateDesiredPower(mCurrentState, mCurrentControlMode);
        output = Util.limit(output, -1, 1); // 10% of the desired power; Used for testing purposes.

        mData.kLoggingTable.putDouble( "Desired Output", output );
        mData.kLoggingTable.putDouble( "Current Drawn", getCurrent() );
        mData.kLoggingTable.putDouble( "Motor Output", mMasterElevator.getAppliedOutput() );

        System.out.printf("Current: %s\tDesired Power: %s\tActual Output: %s\t\n", getCurrent(), output, mMasterElevator.getAppliedOutput());

        if(mCurrentControlMode != EControlMode.MOTION_MAGIC) {
            mMasterElevator.set(output);
        }

        //Logging to smart dashboard for Motion Magic Test
        mData.kSmartDashboard.putDouble("Power", output);
        mData.kSmartDashboard.putDouble("Current", mMasterElevator.getOutputCurrent());
        mData.kSmartDashboard.putDouble("Desired Output", output);
        mData.kSmartDashboard.putDouble("Current Ticks", (double) getCurrentEncoderTicks());
        mData.kSmartDashboard.putString( "Current State", mCurrentState.toString() );
        mData.kSmartDashboard.putString( "Current Control Mode", mCurrentControlMode.toString() );


        mData.elevator.set( EElevator.AT_BOTTOM, isAtBottomVal() );
        mData.elevator.set( EElevator.AT_TOP, isAtTopVal() );
        mData.elevator.set( EElevator.BUS_VOLTAGE, getBusVoltage());
        mData.elevator.set( EElevator.CURRENT, getCurrent());
        mData.elevator.set( EElevator.CURRENT_ENCODER_TICKS, (double) getCurrentEncoderTicks());
        mData.elevator.set( EElevator.CURRENT_NEO_TICKS, getNeoEncoderPosition());
        mData.elevator.set( EElevator.CURRENT_POSITION, (double) getCurrentPosition().ordinal());
        mData.elevator.set( EElevator.CURRENT_STATE, (double) getCurrentState().ordinal());
        mData.elevator.set( EElevator.DESIRED_DIRECTION_UP, desiredDirectionUpVal());
        mData.elevator.set( EElevator.DESIRED_POSITION, (double) getDesiredPosition().ordinal() );
        mData.elevator.set( EElevator.DESIRED_POSITION_ABOVE_INITIAL, desiredPositionAboveInitialVal());
        mData.elevator.set( EElevator.DESIRED_POWER, getDesiredPower());
        mData.elevator.set(EElevator.OUTPUT_POWER, output);
        mData.elevator.set( EElevator.SETTING_POSITION, settingPositionVal());
        mData.elevator.set( EElevator.CURRENT_CONTROL_MODE, (double) getCurrentControlMode().ordinal());

    }

    private void updateElevatorState(double pNow) {

        // Should override any automated control
        if (mCurrentState != EElevatorState.SET_POSITION && mDesiredPower == 0d && !mAtBottom) { // Hold if input is absent and we aren't at the bottom
            mCurrentState = EElevatorState.HOLD;
        } else {
            mCurrentState = EElevatorState.NORMAL;
        }

        if (mDesiredDirectionUp) {
            shouldDecelerate(mCurrentEncoderTicks, mDesiredDirectionUp);
        }

    }

    /**
     * When the elevator reaches the bottom
     * then the current encoder ticks will be set to zero
     * and the encoder will be zeroed.
     */
    private void resetBottom() {
        mCurrentEncoderTicks = 0;
        zeroEncoder();
    }

    /**
     * Sets the measurement of
     * encoder ticks it takes to get to the top
     * to whatever the current encoder tick count is
     */
    private void resetTop() {
        mTopEncoderTicks = mCurrentEncoderTicks;
    }

    /**
     * Resets the encoder
     * which sets the amount of ticks
     * to zero
     */
    public void zeroEncoder() {
        mEncoder.reset();
        mCurrentEncoderTicks = 0;
    }

    /**
     * Checks whether or not the elevator is
     * close enough to the top or to the bottom
     * such that the elevator should be decelerating
     */
    private void shouldDecelerate(double pCurrentEncoderTicks, boolean pCurrentDirectionUp) {
        if (pCurrentDirectionUp) {
            if (pCurrentEncoderTicks >= mTopEncoderTicks) {
                mCurrentState = EElevatorState.DECEL_TOP;
            }
        } else if (!pCurrentDirectionUp) {
            if (pCurrentEncoderTicks >= mBottomEncoderTicks) {
                mCurrentState = EElevatorState.DECEL_BOTTOM;
            }
        }
    }

    /**
     * Takes the desired position and calculates the power required to get there
     * based on the distance between the current position and the desired one
     * using a PID loop.
     */
    private double calculateSetpointPower() {

        double power;

        if(mPidController.isOnTarget(100)) {
            power = 0;
        } else {
            power = -mPidController.calculate(mEncoder.get(), mCurrentTime); //TODO test
        }

        return power;
    }

    public double getCurrentEncoderTicks() {
        return mCurrentEncoderTicks;
    }

    public void setDesiredPower(double pPower) {
        mCurrentState = EElevatorState.NORMAL;
        mDesiredPower = pPower;
    }

    public boolean getDirection() {
        return mDesiredDirectionUp;
    }

    public double getDesiredPower() {
        return mDesiredPower;
    }

    public double getBusVoltage() {
        return mMasterElevator.getBusVoltage();
    }

    public boolean finishedPositioning() {
        return !mSettingPosition;
    }

    public double getEncoderPosition() {
//        return mEncoder.get();
        return mMasterElevator.getEncoder().getPosition();
    }

    public double getCurrent() {
        return mMasterElevator.getOutputCurrent();
    }

    public double getNeoEncoderPosition() {
        return mNeoEncoderPosition;
    }

    public EElevatorPosition getDesiredPosition() {
        return mDesiredPosition;
    }

    public EElevatorPosition getCurrentPosition() {
        return mCurrentPosition;
    }

    public EControlMode getCurrentControlMode() {
        return mCurrentControlMode;
    }

    public double isAtTopVal() {
        if(mAtTop) {
            return 1d;
        }
        return 0d;
    }

    public double isAtBottomVal() {
        if(mAtBottom) {
            return 1d;
        }
        return 0d;
    }

    public double desiredPositionAboveInitialVal() {
        if(mDesiredPositionAboveInitial) {
            return 1d;
        }
        return 0d;
    }

    public double desiredDirectionUpVal() {
        if(mDesiredDirectionUp) {
            return 1d;
        }
        return 0d;
    }

    public double settingPositionVal() {
        if(mSettingPosition) {
           return 1d;
        }
        return 0d;
    }

    public EElevatorState getCurrentState() {
        return mCurrentState;
    }
    /**
     * Returns true if the encoder has reached the upper
     * at of the elevator, which is based on if it is moving up
     * and if it is drawing more current than is acceptable.
     * @return true if the elevator has reached the upper limit, false
     * if not.
     */
    private boolean reachedUpperLimit() {
        return (mEncoder.getDirection() && getCurrent() >= SystemSettings.kElevatorSmartCurrentLimit);
    }

    /**
     * Calculates the desired power based on
     * the current state of the elevator.
     * @param pCurrentState the current state of the elevator
     * @return the calculated power output as a double
     */
    private double calculateDesiredPower(EElevatorState pCurrentState, EControlMode pCurrentControlMode) {

        double power = 0d;

        switch (pCurrentState) {
        case NORMAL:
            power = mDesiredPower; // 10% of the actual power equals what the driver wants
            mSettingPosition = false;
            break;
//        case DECEL_TOP:
//            power = Math.min(mDesiredPower, pCurrentState.getPower()); // Use the lower value to ease into the top
//            break;
//        case DECEL_BOTTOM:
//            power = Math.max(mDesiredPower, pCurrentState.getPower()); // Use the higher value to ease into the bottom
//            break;
//        case HOLD:
//            power = mHoldVoltage / getBusVoltage(); // Need to test
//            break;
        case STOP:
            power = 0;
            break;
        case SET_POSITION:
            switch ( pCurrentControlMode ) {
                case PID:
                    power = calculateSetpointPower(); // Start setting position with the current time
                    break;
                case MOTION_MAGIC:
                    calculateMotionMagic();
                    break;
                    default:
                        break;
            }
            break;
        default:
            System.out.println("Somehow reached an unaccounted state with " + pCurrentState.toString()); // In case,
                                                                                                         // somehow, we
                                                                                                         // reach this
        }

        return Util.limit(power, mMinPower, mMaxPower);

    }



    // This method is made to be called from outside the class
    // the state should only reach set point when driver input call it
    public void setDesirecPosition(EElevatorPosition pDesiredPosition) {
        mCurrentState = EElevatorState.SET_POSITION;
        mDesiredPosition = pDesiredPosition;
        mPidController.setSetpoint(pDesiredPosition.mEncoderThreshold()); // Our set point is the threshold of the
        // destination state
        mSettingPosition = true; // Keeps track that we are in the process of setting the position
    }



    public void logInfo() {
    }

    private void calculateMotionMagic() {
        double setPoint = mDesiredPosition.mEncoderThreshold();
        mCanController.setReference( setPoint, ControlType.kSmartMotion ); //Maybe should be a velocity control type..?
    }

    public void setControlMode(EControlMode pControlMode) {
        this.mCurrentControlMode = pControlMode;
    }

    public enum EControlMode {
        MOTION_MAGIC,
        PID
    }

}