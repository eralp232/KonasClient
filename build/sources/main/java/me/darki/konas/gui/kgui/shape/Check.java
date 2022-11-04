package me.darki.konas.gui.kgui.shape;

import org.lwjgl.util.vector.Vector2f;

public class Check extends AbstractShape {
    protected float width;
    protected float height;

    public Check(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        createPoints();
        checkPoints();
    }

    @Override
    protected void createPoints() {
        points = new Vector2f[3];

        points[0] = new Vector2f((4F/18F) * width + x, (9F/18F) * height + y);

        points[1] = new Vector2f((8F/18F) * width + x, (13F/18F) * height + y);

        points[2] = new Vector2f((14F/18F) * width + x, (5F/18F) * height + y);

    }

    @Override
    public boolean isClosed() {
        return false;
    }
}
