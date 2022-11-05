package me.darki.konas.gui.kgui.fill;

import me.darki.konas.gui.kgui.shape.AbstractShape;
import me.darki.konas.setting.ColorSetting;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class GradientFill implements Fill {
    private final Vector2f start;
    private final Vector2f end;

    private Color startCol;
    private Color endCol;

    private ColorSetting startColSetting = null;
    private ColorSetting endColSetting = null;

    public GradientFill(float x, float y, float width, float height, Color start, Color end) {
        this.start = new Vector2f(x, y);
        this.end = new Vector2f(x + width, y + height);
        this.startCol = start;
        this.endCol = end;
    }

    public GradientFill(float x, float y, float width, float height, ColorSetting start, ColorSetting end) {
        this.start = new Vector2f(x, y);
        this.end = new Vector2f(x + width, y + height);
        this.startColSetting = start;
        this.endColSetting = end;
        this.startCol = start.getColorObject();
        this.endCol = end.getColorObject();
    }

    public GradientFill brighten(float amount) {
        if (startColSetting != null && endColSetting != null) {
            startCol = startColSetting.getColorObject();
            endCol = endColSetting.getColorObject();
        }

        return new GradientFill(start.getX(), start.getY(), end.getX() - start.getX(), end.getY() - start.getY(), brighten(startCol, amount), brighten(endCol, amount));
    }

    private Color brighten(Color color, float amount) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int alpha = color.getAlpha();

        int i = (int)(1.0/(1.0-amount));
        if ( r == 0 && g == 0 && b == 0) {
            return new Color(i, i, i, alpha);
        }
        if ( r > 0 && r < i ) r = i;
        if ( g > 0 && g < i ) g = i;
        if ( b > 0 && b < i ) b = i;

        return new Color(Math.min((int)(r/amount), 255),
                Math.min((int)(g/amount), 255),
                Math.min((int)(b/amount), 255),
                alpha);
    }

    @Override
    public Color colorAt(AbstractShape abstractShape, float x, float y) {
        if (startColSetting != null && endColSetting != null) {
            startCol = startColSetting.getColorObject();
            endCol = endColSetting.getColorObject();
        }

        float dx1 = end.getX() - start.getX();
        float dy1 = end.getY() - start.getY();

        float dx2 = -dy1;
        float dy2 = dx1;
        float denom = (dy2 * dx1) - (dx2 * dy1);

        if (denom == 0) {
            return Color.BLACK;
        }

        float ua = (dx2 * (start.getY() - y)) - (dy2 * (start.getX() - x));
        ua /= denom;
        float ub = (dx1 * (start.getY() - y)) - (dy1 * (start.getX() - x));
        ub /= denom;
        float u = ua;
        if (u < 0) {
            u = 0;
        }
        if (u > 1) {
            u = 1;
        }
        float v = 1 - u;

        // u is the proportion down the line we are
        int r = (int) ((u * endCol.getRed()) + (v * startCol.getRed()));
        int b = (int) ((u * endCol.getBlue()) + (v * startCol.getBlue()));
        int g = (int) ((u * endCol.getGreen()) + (v * startCol.getGreen()));
        int a = (int) ((u * endCol.getAlpha()) + (v * startCol.getAlpha()));

        return new Color(r, g, b, a);
    }
}
