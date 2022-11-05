package me.darki.konas.util.render;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

public class CrosshairRenderer {
    public static void preRender() {
        GL11.glDisable(GL_TEXTURE_2D);
        GL11.glBlendFunc(770, 771);
    }

    public static void postRender() {
        GL11.glEnable(GL_TEXTURE_2D);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawLine(float x1, float y1, float x2, float y2, float thickness, Color colour, boolean smooth) {
        drawLines(new float[] { x1, y1, x2, y2 }, thickness, colour, smooth);
    }

    public static void drawRectangle(float x1, float y1, float x2, float y2, float thickness, Color colour, boolean smooth) {
        drawLines(new float[] {
                x1, y1, x2, y1, x2, y1, x2, y2, x1, y2,
                x2, y2, x1, y1, x1, y2 }, thickness, colour, smooth);
    }

    public static void drawLines(float[] points, float thickness, Color colour, boolean smooth) {
        preRender();
        if (smooth) {
            GL11.glEnable(GL_LINE_SMOOTH);
        } else {
            GL11.glDisable(GL_LINE_SMOOTH);
        }
        GL11.glLineWidth(thickness);
        GL11.glColor4f(colour.getRed() / 255.0F, colour.getGreen() / 255.0F, colour.getBlue() / 255.0F, colour.getAlpha() / 255.0F);
        GL11.glBegin(GL_LINES);
        for (int i = 0; i < points.length; i += 2)
            GL11.glVertex2f(points[i], points[i + 1]);
        GL11.glEnd();
        postRender();
    }

    public static void drawFilledRectangle(float x1, float y1, float x2, float y2, Color colour, boolean smooth) {
        drawFilledShape(new float[] { x1, y1, x1, y2, x2, y2, x2, y1 }, colour, smooth);
    }

    public static void drawFilledShape(float[] points, Color colour, boolean smooth) {
        preRender();
        if (smooth) {
            GL11.glEnable(GL_LINE_SMOOTH);
        } else {
            GL11.glDisable(GL_LINE_SMOOTH);
        }
        GL11.glColor4f(colour.getRed() / 255.0F, colour.getGreen() / 255.0F, colour.getBlue() / 255.0F, colour.getAlpha() / 255.0F);
        GL11.glBegin(GL_POLYGON);
        for (int i = 0; i < points.length; i += 2)
            GL11.glVertex2f(points[i], points[i + 1]);
        GL11.glEnd();
        postRender();
    }

    public static void drawPartialCircle(float x, float y, float radius, int startAngle, int endAngle, float thickness, Color colour, boolean smooth) {
        preRender();
        if (startAngle > endAngle) {
            int temp = startAngle;
            startAngle = endAngle;
            endAngle = temp;
        }
        if (startAngle < 0)
            startAngle = 0;
        if (endAngle > 360)
            endAngle = 360;
        if (smooth) {
            GL11.glEnable(GL_LINE_SMOOTH);
        } else {
            GL11.glDisable(GL_LINE_SMOOTH);
        }
        GL11.glLineWidth(thickness);
        GL11.glColor4f(colour.getRed() / 255.0F, colour.getGreen() / 255.0F, colour.getBlue() / 255.0F, colour.getAlpha() / 255.0F);
        GL11.glBegin(GL_LINE_STRIP);
        float ratio = 0.01745328F;
        for (int i = startAngle; i <= endAngle; i++) {
            float radians = (i - 90) * ratio;
            GL11.glVertex2f(x + (float)Math.cos(radians) * radius, y + (float)Math.sin(radians) * radius);
        }
        GL11.glEnd();
        postRender();
    }

    public static void drawString(String text, int x, int y, Color colour) {
        GL11.glColor4f(colour.getRed() / 255.0F, colour.getGreen() / 255.0F, colour.getBlue() / 255.0F, colour.getAlpha() / 255.0F);
        (Minecraft.getMinecraft()).fontRenderer.drawString(text, x, y, 0);
    }
}
