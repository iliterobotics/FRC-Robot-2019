package us.ilite.robot.modules;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import com.team254.lib.drivers.talon.TalonSRXFactory;

import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import us.ilite.common.Data;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.types.manipulator.EIntake;
import us.ilite.common.types.sensor.EPowerDistPanel;
import us.ilite.robot.driverinput.DriverInput.EGamePiece;


public class Intake extends Module {

    // TODO update allowed error angle
    private final double kZero = 0.0;
    private final String kDefaultError = "This message should not be displayed";

    // Logging fields
    private ILog mLog = Logger.createLog(Intake.class);
    private Data mData;

    // Roller fields
    private VictorSPX mIntakeRoller;
    private double mIntakeRollerCurrent;
    private double mIntakeRollerVoltage;

    // Wrist/Arm fields - keep a reference to the talon so we can power output 'off'
    private MotionMagicArm mWrist;
    private TalonSRX mWristTalon;

    // Pneumatic/Solenoid for hatch/cargo roller mode toggle
    private Solenoid mSolenoid;

    // Present intake states
    private EIntakeState mDesiredIntakeState;
    private EGamePiece mGamePiece;

    public Intake(Data pData) {
        // Basic Construction
        mIntakeRoller = new VictorSPX(SystemSettings.kCargoIntakeSPXLowerAddress);
        this.mData = pData;
    
        // Wrist control
        mWristTalon = TalonSRXFactory.createDefaultTalon(SystemSettings.kIntakeWristSRXAddress);
        mWrist = new MotionMagicArm(mWristTalon);

        // Solenoid for changing between cargo and hatch mode
        mSolenoid = new Solenoid(SystemSettings.kCANAddressPCM, SystemSettings.kIntakeSolenoidAddress);
        mDesiredIntakeState = EIntakeState.STOWED;
    }


    @Override
    public void modeInit(double pNow) {
        mLog.error("MODE INIT");
        mDesiredIntakeState = EIntakeState.STOWED;
        mGamePiece = EGamePiece.HATCH;
        mWrist.modeInit(pNow);
    }

    @Override
    public void periodicInput(double pNow) {
        mData.intake.set(EIntake.ARM_ANGLE, mWrist.getCurrentArmAngle());
        mData.intake.set(EIntake.ENCODER_TICKS, (double)mWristTalon.getSelectedSensorPosition());
        mData.intake.set(EIntake.ENCODER_VEL_TICKS, (double)mWristTalon.getSelectedSensorVelocity());
        mData.intake.set(EIntake.ROLLER_CURRENT, mIntakeRollerCurrent);
        mData.intake.set(EIntake.ROLLER_VOLTAGE, mIntakeRollerVoltage);
        mData.intake.set(EIntake.SOLENOID_EXTENDED, mSolenoid.get() ? 1.0 : 0.0);

        
        mWrist.periodicInput(pNow);
    }

    @Override
    public void update(double pNow) {
        // mWrist.setArmPower(power);
        // EPowerDistPanel ID 11 (CURRENT11) corresponds to Intake Rollers
        mIntakeRollerCurrent = mData.pdp.get(EPowerDistPanel.CURRENT11);
        mIntakeRollerVoltage = mIntakeRoller.getMotorOutputVoltage();

        mData.kSmartDashboard.putDouble("Intake Wrist Angle", mWrist.getCurrentArmAngle());
        SmartDashboard.putNumber("Arm kF", 0.3 / ((double)mWristTalon.getSelectedSensorVelocity()));

        // TODO put this into each case in IntakeState to prevent unwanted solenoid extension
        setSolenoidState(mGamePiece);        

        switch (mDesiredIntakeState) {
            case GROUND:
                //if (mWristAngle < SystemSettings.kIntakeWristGroundMinBound) break;
                setRollerState(ERollerState.CARGO);
                mWrist.setArmAngle(mDesiredIntakeState.kWristAngleDegrees);
                break;
            case HANDOFF:
                setRollerState(ERollerState.HOLD);
                mWrist.setArmAngle(mDesiredIntakeState.kWristAngleDegrees);
                break;
            case STOWED:
                setSolenoidState(EGamePiece.HATCH);
                setRollerState(ERollerState.STOPPED);
                mWrist.setArmAngle(mDesiredIntakeState.kWristAngleDegrees);
                break;
            case STOPPED: 
            default:
                setRollerState(ERollerState.STOPPED);
                mWristTalon.set(ControlMode.PercentOutput, 0d);
                break;
        }

        mWrist.update(pNow);
        //mWristTalon.set(ControlMode.PercentOutput, this.power);
    }

    /**
     * Main commands that can be used in DriverInput
     */
    public void setIntakeState(EIntakeState pIntakeState) {
        mDesiredIntakeState = pIntakeState;
    }

    private double power = 0d;
    public void overridePower(double pPower){
        power = pPower;
        mWrist.setDesiredOutput(pPower);
    }

    /**
     * Main method for controlling the rollers.
     * Roller power is based on current solenoid state (HATCH/CARGO).
     */
    private void setRollerState(ERollerState pRollerState) {
        setRollerPower(pRollerState.kPower);
    }

    /**
     * Starts the intake roller.
     * @param pPower percent of power to use
     */
    private void setRollerPower(double pPower) {
        mIntakeRoller.set(ControlMode.PercentOutput, pPower);
    }

    /**
     * Sets the solenoid state.
     * i.e. sets whether the intake is in "cargo mode" or "hatch mode".
     */
    private void setSolenoidState(EGamePiece pGamePiece) {
        switch(pGamePiece) {
            case HATCH:
                mSolenoid.set(ESolenoidState.HATCH.kExtended);
                break;
            case CARGO:
                mSolenoid.set(ESolenoidState.CARGO.kExtended);
                break;
            default:
                mLog.error(kDefaultError);
        }
        
    }

    public void setGamePiece(EGamePiece pGamePiece) {
        mGamePiece = pGamePiece;
    }

    // /**
    //  * Asks if the a given wrist position matches the current wrist position.
    //  * @param pWristPosition position in question
    //  * @return Whether the wrist had been at the specified setpoint for a certain amount of time within a certain deadband.
    //  */
    // public boolean isAtPosition(EIntakeState pWristPosition) {
    //     return true;
    // }

    @Override
    public boolean checkModule(double pNow) {
        return mWrist.checkModule(pNow);
    }

    @Override
    public void shutdown(double pNow) {
        mWrist.shutdown(pNow);
    }



    //*****************************************************//
    //                     Enumerators                     //
    //*****************************************************//

    private enum ESolenoidState {
        HATCH(false),
        CARGO(true);

        public final boolean kExtended;

        private ESolenoidState(boolean pExtended) {
            kExtended = pExtended;
        }
    }

    /**
     * Roller state
     */
    public enum ERollerState {
        STOPPED(0.0),
        HATCH(SystemSettings.kIntakeRollerHatchPower),
        CARGO(SystemSettings.kIntakeRollerCargoPower),
        HOLD(SystemSettings.kIntakeRollerHoldPower);

        public final double kPower;

        private ERollerState(double pPower) {
            this.kPower = pPower;
        }
    }

    public boolean hasReachedState(EIntakeState pIntakeState) {
        return mDesiredIntakeState.equals( pIntakeState );
    }

    public enum EIntakeState {
        GROUND(SystemSettings.kIntakeWristGroundAngle),
        HANDOFF(SystemSettings.kIntakeWristHandoffAngle), 
        STOWED(SystemSettings.kIntakeWristStowedAngle),
        STOPPED(0);

        // TODO update wrist position angles
        public final double kWristAngleDegrees;

        EIntakeState(double pWristAngle) {
            kWristAngleDegrees = pWristAngle;
        }
    }

}
