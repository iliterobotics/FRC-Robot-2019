package us.ilite.display.simulation.ui;

import javafx.scene.image.Image;
import us.ilite.common.types.input.ELogitech310;

public class KeyImage {

    private Image mNormalImage;
    private Image mPressedImage;
    private Image mCurrentImage;
    private double displayRate;
    private double xCoodrniate, yCoordinate;


    public KeyImage(Image pNormalImage, Image pPressedImage, double displayRate, double xCoodrniate, double yCoordinate) {
        this.mNormalImage = pNormalImage;
        this.mPressedImage = pPressedImage;
        this.displayRate = displayRate;
        this.xCoodrniate = xCoodrniate;
        this.yCoordinate = yCoordinate;
        this.mCurrentImage = mNormalImage;
    }
    
    public void setImage(Image pToImage) {
        mCurrentImage = pToImage;
    }

    public Image getImage() {
        return mCurrentImage;
    }

    public Image pressedImage() {
        return mPressedImage;
    }

    public Image NormalImage() {
        return mNormalImage;
    }

    public void stopBlinking(double pRefreshRate) {
        displayRate = pRefreshRate;
    }

    public void blink(double pRefreshRate) {
        displayRate = pRefreshRate - 20;
    }

    public double getDisplayRate() {
        return displayRate;
    }

    public double getX() {
        return xCoodrniate;
    }

    public double getY() {
        return yCoordinate;
    }
    public double setX(double val) {
        xCoodrniate = val;
        return xCoodrniate;
    }

    public double setY(double val) {
        yCoordinate = val;
        return yCoordinate;
    }

    public double setXY(double x, double y) {
        setY(y);
        setX(x);
        return x;
    }
}