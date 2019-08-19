package us.ilite.robot.modules;

import com.ctre.phoenix.CANifier;

import edu.wpi.first.wpilibj.Timer;
import us.ilite.common.Data;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.types.ETargetingData;
import us.ilite.common.types.ETrackingType;

public class LEDControl extends Module {

    private CANifier mCanifier;
    private Timer mBlinkTimer;
    private boolean mLedOn;
    private Message mCurrentMessage;
    
    private final Drive mDrive;
//    private final Elevator mElevator;
//    private final PneumaticIntake mPneumaticIntake;
//    private final CargoSpit mCargoSpit;
    private final HatchFlower mHatchFlower;
    private final FourBar mFourBar;
    private final Limelight mLimelight;
    private final Data mData;

    
    public static class RGB {
        private int mR;
        private int mG;
        private int mB;

        public RGB(int pR, int pG, int pB) {
            // Value range for each color is 0-255, we'll enforce this with a modulo divide
            this.mR = pR % 256;
            this.mG = pG % 256;
            this.mB = pB % 256;
        }

        // getters for integer RGB
        public int getR() {
			return this.mR;
		}

        public int getG() {
			return this.mG;
		}

        public int getB() {
			return this.mB;
		}

        // getters for double RGB
        public double getRPercent() {
			return ((double) this.mR) / 256.0;
		}

        public double getGPercent() {
			return ((double) this.mG) / 256.0;
		}

        public double getBPercent() {
			return ((double) this.mB) / 256.0;
		}

    }


    public enum LEDColor {
        PURPLE( 255, 0, 200 ),
        RED( 255, 0, 0 ),
        LIGHT_BLUE( 0, 100, 220 ),
        WHITE( 255, 255, 255 ),
        GREEN( 0, 255, 0 ),
        YELLOW( 255, 255, 0 ),
        GREEN_HSV( 84, 255, 255 ),
        BLUE( 0, 0, 255 ),
        RED_HSV( 0, 255, 255 ),
        YELLOW_HSV( 20, 255, 255 ),
        PURPLE_HSV( 212, 255, 255 ),
        ORANGE( 255, 165, 0 ),
        NONE( 0, 0, 0 );

        private RGB rgb;

        LEDColor( int pR, int pG, int pB ) {
            this.rgb = new RGB(pR, pG, pB);
        }

        public RGB getColor() {
            return this.rgb;
        }
    }


    // pulse speed in milliseconds, 0 = on solid
    public enum Message {
        HAS_HATCH( LEDColor.YELLOW, 0 ),
        HAS_CARGO( LEDColor.ORANGE, 0 ),
        CURRENT_LIMITING( LEDColor.RED, 300 ),
        VISION_TRACKING( LEDColor.GREEN, 0 ),
        KICKING_HATCH( LEDColor.BLUE, 0 ),
        SPITTING_CARGO( LEDColor.WHITE, 0 ),
        NONE( LEDColor.NONE, 0 );

        final LEDColor color;
        final int pulse; // milliseconds

        Message( LEDColor color, int pulse ) {
            this.color = color;
            this.pulse = pulse;
        }
    }

    public LEDControl(Drive mDrive,/* Elevator mElevator, */ /*CargoSpit mCargoSpit, */HatchFlower mHatchFlower, FourBar mFourBar, Limelight mLimelight, Data mData) {
        this.mDrive = mDrive;
//        this.mElevator = mElevator;
//        this.mPneumaticIntake = mPneumaticIntake;
//        this.mCargoSpit = mCargoSpit;
        this.mHatchFlower = mHatchFlower;
        this.mFourBar = mFourBar;
        this.mLimelight = mLimelight;
        this.mData = mData;

        mCanifier = new CANifier(SystemSettings.kCanifierAddress);
        this.mCurrentMessage = Message.NONE;
        this.mLedOn = true;

        this.mBlinkTimer = new Timer();
        this.mBlinkTimer.reset();
    }


    @Override
    public void modeInit(double pNow) {
        this.turnOffLED();
        this.mCurrentMessage = Message.NONE;
        this.mLedOn = true;
        this.mBlinkTimer.stop();
        this.mBlinkTimer.reset();
    }


    /**
     * Updates LED strip based on mechanism states. We check mechanisms in order of lowest to highest priority.
     */
    public void update(double pNow) {
        Message lastMsg = this.mCurrentMessage;
        this.mCurrentMessage = Message.NONE;
        
        if(CargoSpitSingle.getInstance().isCurrentLimiting()) mCurrentMessage = Message.CURRENT_LIMITING;
        if(ElevatorSingle.getInstance().isCurrentLimiting()) mCurrentMessage = Message.CURRENT_LIMITING;
        if(mDrive.isCurrentLimiting()) mCurrentMessage = Message.CURRENT_LIMITING;
        if(mFourBar.isCurrentLimiting()) mCurrentMessage = Message.CURRENT_LIMITING;
        
        if(CargoSpitSingle.getInstance().hasCargo()) mCurrentMessage = Message.HAS_CARGO;
        if(mHatchFlower.hasHatch()) mCurrentMessage = Message.HAS_HATCH;

        if(CargoSpitSingle.getInstance().isOuttaking()) mCurrentMessage = Message.SPITTING_CARGO;
        if(mHatchFlower.shouldBackUp()) mCurrentMessage = Message.KICKING_HATCH;

        if(mLimelight.getTracking() != ETrackingType.NONE) mCurrentMessage = Message.VISION_TRACKING;

        // Did the message change?
        if ( lastMsg != this.mCurrentMessage ) {
            // The message changed, reset the timer and on state
            this.mLedOn = true;
            this.mBlinkTimer.stop();
            this.mBlinkTimer.reset();
            this.mBlinkTimer.start();
        }

        controlLED(mCurrentMessage);
    }


    public void controlLED(Message m)
    {
        // Timer wants elapsed time in double seconds, pulse period specified in ms.
        double blinkPeriod = ((double) m.pulse) / 1000.0;

        if(m.pulse == 0)
        {
            mLedOn = true;
        }
        else if( this.mBlinkTimer.hasPeriodPassed(blinkPeriod) ) {
            mLedOn = !mLedOn;
            this.mBlinkTimer.stop();
            this.mBlinkTimer.reset();
            this.mBlinkTimer.start();
        }

        if(mLedOn) {
            setLED(m.color);
        } else {
            turnOffLED();
        }

    }


    private void setLED(LEDColor color) {
        setLED(color.getColor());
    }

    // LED Channels: A = Green B = Red C = Blue
    private void setLED(RGB rgb)
    {
        mCanifier.setLEDOutput(rgb.getRPercent(), CANifier.LEDChannel.LEDChannelB); // Red
        mCanifier.setLEDOutput(rgb.getGPercent(), CANifier.LEDChannel.LEDChannelA); // Green
        mCanifier.setLEDOutput(rgb.getBPercent(), CANifier.LEDChannel.LEDChannelC); // Blue
    }


    public void turnOffLED()
    {
        setLED(LEDColor.NONE);
    }


    public void shutdown(double pNow) {
        // TODO Auto-generated method stub
        this.turnOffLED();
        this.mBlinkTimer.stop();
        this.mBlinkTimer.reset();
    }


    @Override
    public void periodicInput(double pNow) {

    }

}