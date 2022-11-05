package me.darki.konas.util.math;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class RoundingUtil {

    public static double roundDouble(double value, int places) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static double roundToStep(double value, double step) {
        return step * Math.round(value * (1 / step));
    }

    public static float roundFloat(float value, int places) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.floatValue();
    }

    public static float roundToStep(float value, float step) {
        return step * Math.round(value * (1 / step));
    }

}
