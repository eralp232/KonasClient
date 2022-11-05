package me.darki.konas.gui.kgui.shape;

import org.lwjgl.util.vector.Vector2f;

public class AbstractShape {
    protected Vector2f[] points;
    protected Vector2f center;

    protected float x;
    protected float y;

    protected float maxX;
    protected float maxY;
    protected float minX;
    protected float minY;

   protected final void checkPoints() {
       if (points.length > 0) {
           maxX = points[0].x;
           maxY = points[0].y;
           minX = points[0].x;
           minY = points[0].y;
           for (Vector2f point : points) {
               maxX = Math.max(point.x, maxX);
               maxY = Math.max(point.y, maxY);
               minX = Math.min(point.x, minX);
               minY = Math.min(point.y, minY);
           }
           findCenter();
       }
   }

    protected void findCenter() {
        center = new Vector2f(0, 0);
        int length = points.length;
        for (Vector2f point : points) {
            center.x += point.x;
            center.y += point.y;
        }
        center.x /= ((float) length / 2);
        center.y /= ((float) length / 2);
    }

    protected void createPoints() {}

    public Vector2f[] getPoints() {
        return points;
    }

    public Vector2f getCenter() {
        return center;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getMaxX() {
        return maxX;
    }

    public float getMaxY() {
        return maxY;
    }

    public float getMinX() {
        return minX;
    }

    public float getMinY() {
        return minY;
    }

    public boolean isClosed() {
        return false;
    }
}
