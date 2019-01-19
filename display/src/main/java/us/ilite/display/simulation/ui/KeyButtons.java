package us.ilite.display.simulation.ui;

import javafx.scene.image.Image;
import us.ilite.common.types.input.ELogitech310;

public class KeyButtons {

    private Image mImg;
    private ELogitech310 mValue;

    public KeyButtons( Image pImg, ELogitech310 pValue ) {
        this.mImg = pImg;
        this.mValue = pValue;
    }
    public KeyButtons( Image pImg ) {
        this.mImg = pImg;
        this.mValue = ELogitech310.A_BTN;
    }

    public Image getImage() {
        return mImg;
    }

    public ELogitech310 getValue() {
        return mValue;
    }


}