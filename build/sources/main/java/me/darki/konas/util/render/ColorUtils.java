package me.darki.konas.util.render;

import java.awt.*;

public class ColorUtils {

    /*public static float[] intToRGB(int color) {
        *//*int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = (color) & 0xFF;
        int alpha = (color >> 24) & 0xFF;
        return new int[]{red, green, blue, alpha};*//*
        Color c = new Color(color);
        return new float[]{c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f};
    }*/

    public static float[] intToRGB(int color) {

        float alpha = ((color >> 24) & 0xFF) / 255f;
        float red = ((color >> 16) & 0xFF) / 255f;
        float green = ((color >> 8) & 0xFF) / 255f;
        float blue = ((color & 0xFF)) / 255f;

        return new float[]{red, green, blue, alpha};
    }

    public static int rainbow(int delay, float[] hsb) {
        double rainbowState = Math.ceil((System.currentTimeMillis() + delay) / 20.0);
        rainbowState %= 360;
        return Color.getHSBColor((float) (rainbowState / 360.0f), hsb[1], hsb[2]).getRGB();
    }

    public static int pulse(int delay, float[] hsb, float spread, float speed, float range) {
        double sin = Math.sin(spread * ((System.currentTimeMillis() / Math.pow(10, 2)) * (speed / 10) + delay));
        sin *= range;
        return Color.getHSBColor(hsb[0], hsb[1], (float) ((sin + 1) / 2) + ((1F -range) * 0.5F)).getRGB();
    }

}
