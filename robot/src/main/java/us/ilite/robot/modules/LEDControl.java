package us.ilite.robot.modules;

import com.ctre.phoenix.CANifier;
import us.ilite.common.types.ETrackingType;

public class LEDControl {

    private CANifier mCanifier;
    private long blinkStartTime;
    private boolean isOn;
    private Message mCurrentMessage;
    private Elevator mElevator;
    private Intake mIntake;
    private HatchFlower mHatchFlower;
    private CargoSpit mCargoSpit;
    private Limelight mLimelight;

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

        final int mR, mG, mB;

        LEDColor( int pR, int pG, int pB ) {
            this.mR = pR;
            this.mG = pG;
            this.mB = pB;
        }
    }

    //pulse speed = 100, slow flash = 300, solid = 0
    public enum Message {
        HAS_HATCH( LEDColor.YELLOW, 0 ),
        HAS_CARGO( LEDColor.ORANGE, 0 ),
        CURRENT_LIMITING( LEDColor.RED, 100 ),
        VISION_TRACKING( LEDColor.GREEN, 0 ),
        KICKING_HATCH( LEDColor.BLUE, 0 ),
        NONE( LEDColor.NONE, 0 );

        final LEDColor color;
        final int delay;

        Message( LEDColor color, int delay ) {
            this.color = color;
            this.delay = delay;
        }
    }


    public LEDControl(Intake pIntake, Elevator pElevator, HatchFlower pHatchFlower, CargoSpit pCargoSpit, Limelight pLimelight)
    {
        mIntake = pIntake;
        mElevator = pElevator;
        mHatchFlower = pHatchFlower;
        mCargoSpit = pCargoSpit;
        mLimelight = pLimelight;
        this.isOn = true;
    }
    public void initialize(double pNow) {
        mCurrentMessage = Message.NONE;
        blinkStartTime = System.currentTimeMillis();
    }

    /**
     * Updates LED strip based on mechanism states. We check mechanisms in order of lowest to highest priority.
     */
    public boolean update(double pNow) {
        mCurrentMessage = Message.NONE;
        if(mHatchFlower.hasHatch()) mCurrentMessage = Message.HAS_HATCH;
        if(mCargoSpit.hasCargo()) mCurrentMessage = Message.HAS_CARGO;
        if(mLimelight.getTracking() != ETrackingType.NONE) mCurrentMessage = Message.VISION_TRACKING;
        setLED(mCurrentMessage);
        return false;
    }


    // A = Green B = Red C = Blue
    private double[] colorCreator(LEDColor color)
    {
        //order = grb
        double[] rgb = new double[3];
        rgb[0] = (double)color.mG / 256;
        rgb[1] = (double)color.mR / 256;
        rgb[2] = (double)color.mB / 256;
        return rgb;
    }

    //will be obsolete
    public void setLED(double r, double g, double b)
    {
        setLED(new double[] {g, r, b});
    } //TODO order?

    public void setLED(Message m)
    {
        setLED(colorCreator(m.color), m.delay);
    }
    private void setLED(double[] rgb)
    {
        mCanifier.setLEDOutput(rgb[0], CANifier.LEDChannel.LEDChannelA);
        mCanifier.setLEDOutput(rgb[1], CANifier.LEDChannel.LEDChannelB);
        mCanifier.setLEDOutput(rgb[2], CANifier.LEDChannel.LEDChannelC);
    }

    private void setLED(double[] rgb, long blinkDelay)
    {
        if(blinkDelay == 0)
        {
            isOn = true;
        }
        else if(System.currentTimeMillis() - blinkStartTime > blinkDelay) {
            isOn = !isOn;
            blinkStartTime = System.currentTimeMillis();
        }
        if(isOn) {
            setLED(rgb);
        } else {
            turnOffLED();
        }
    }

    public void turnOffLED()
    {
        setLED(new double[] {0, 0, 0});
    }

    public void shutdown(double pNow) {
        // TODO Auto-generated method stub

    }

}