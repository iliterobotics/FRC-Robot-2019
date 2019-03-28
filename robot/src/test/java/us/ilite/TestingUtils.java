package us.ilite;

import com.flybotix.hfr.codex.Codex;
import us.ilite.common.types.input.ELogitech310;

public class TestingUtils {

    public static void fillNonButtons(Codex<Double, ELogitech310> pCodex, Double value) {
        for(ELogitech310 l : ELogitech310.values()) {
            if(!l.name().toLowerCase().contains("btn")) {
                pCodex.set(l, value);
            }
        }
    }

}
