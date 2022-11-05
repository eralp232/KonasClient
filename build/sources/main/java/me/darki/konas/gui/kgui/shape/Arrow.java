package me.darki.konas.gui.kgui.shape;

import org.lwjgl.util.vector.Vector2f;

public class Arrow extends AbstractShape {
    protected float width;
    protected float height;
    private final boolean upwards;

    public Arrow(float x, float y, float width, float height, boolean upwards) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.upwards = upwards;
        createPoints();
        checkPoints();
    }

    @Override
    protected void createPoints() {
        points = new Vector2f[3];

        if (upwards) {
            points[0] = new Vector2f(0 + x, height + y);

            points[1] = new Vector2f(0.5F * width + x, y);

            points[2] = new Vector2f(width + x, height + y);
        } else {
            points[0] = new Vector2f(0 + x, y);

            points[1] = new Vector2f(0.5F * width + x, height + y);

            points[2] = new Vector2f(width + x, y);
        }

    }

    @Override
    public boolean isClosed() {
        return false;
    }
}