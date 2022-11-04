package me.darki.konas.gui.kgui.shape;

import org.lwjgl.util.vector.Vector2f;
import org.newdawn.slick.util.FastTrig;

import java.util.ArrayList;
import java.util.List;

public class RoundedRectangle extends AbstractShape {
    public static final int TOP_LEFT  = 1;
    public static final int TOP_RIGHT = 2;
    public static final int BOTTOM_RIGHT = 4;
    public static final int BOTTOM_LEFT = 8;
    public static final int ALL = TOP_LEFT | TOP_RIGHT | BOTTOM_RIGHT | BOTTOM_LEFT;

    private static final int DEFAULT_SEGMENT_COUNT = 25;

    protected float width;
    protected float height;

    private float cornerRadius;
    private int segmentCount;
    private int cornerFlags;

    public RoundedRectangle(float x, float y, float width, float height, float cornerRadius) {
        this(x, y, width, height, cornerRadius, DEFAULT_SEGMENT_COUNT);
    }

    public RoundedRectangle(float x, float y, float width, float height, float cornerRadius, int segmentCount) {
        this(x,y,width,height,cornerRadius,segmentCount,ALL);
    }

    public RoundedRectangle(float x, float y, float width, float height, float cornerRadius, int segmentCount, int cornerFlags) {
        if(cornerRadius < 0) {
            throw new IllegalArgumentException("corner radius must be >= 0");
        }

        this.cornerRadius = cornerRadius;
        this.segmentCount = segmentCount;
        this.cornerFlags = cornerFlags;

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        createPoints();
        checkPoints();
    }

    @Override
    protected void createPoints() {
        ArrayList<Vector2f> tempPoints = new ArrayList<>();

        if (cornerRadius > width / 2F) {
            cornerRadius = width / 2F;
        }

        if (cornerRadius > height / 2F) {
            cornerRadius = height / 2F;
        }

        if ((cornerFlags & TOP_LEFT) != 0) {
            tempPoints.addAll(createPoints(segmentCount, cornerRadius, x + cornerRadius, y + cornerRadius, 270, 180));
        } else {
            tempPoints.add(new Vector2f(x, y));
        }

        if ((cornerFlags & BOTTOM_LEFT) != 0) {
            tempPoints.addAll(createPoints(segmentCount, cornerRadius, x + cornerRadius, y + height - cornerRadius, 180, 90));
        } else {
            tempPoints.add(new Vector2f(x, y + height));
        }

        if ((cornerFlags & BOTTOM_RIGHT) != 0) {
            tempPoints.addAll(createPoints(segmentCount, cornerRadius, x + width - cornerRadius, y + height - cornerRadius, 90, 0));
        } else {
            tempPoints.add(new Vector2f(x+width, y+height));
        }

        if ((cornerFlags & TOP_RIGHT) != 0) {
            tempPoints.addAll(createPoints(segmentCount, cornerRadius, x + width - cornerRadius, y + cornerRadius, 360, 270));
        } else {
            tempPoints.add(new Vector2f(x+width, y));
        }

        points = new Vector2f[tempPoints.size()];
        for(int i=0;i<tempPoints.size();i++) {
            points[i] = tempPoints.get(i);
        }
    }

    private List createPoints(int numberOfSegments, float radius, float cx, float cy, float start, float end) {
        ArrayList tempPoints = new ArrayList();

        int step = 360 / numberOfSegments;

        for (float a=start;a>=end+step;a-=step) {
            float ang = a;
            if (ang < end) {
                ang = end;
            }
            float x = (float) (cx + (FastTrig.cos(Math.toRadians(ang)) * radius));
            float y = (float) (cy + (FastTrig.sin(Math.toRadians(ang)) * radius));

            tempPoints.add(new Vector2f(x, y));
        }

        return tempPoints;
    }

    @Override
    public boolean isClosed() {
        return true;
    }
}
