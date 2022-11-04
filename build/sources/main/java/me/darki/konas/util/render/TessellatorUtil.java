package me.darki.konas.util.render;

import me.darki.konas.setting.ColorSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL32;

import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glHint;

public class TessellatorUtil {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static void drawBoundingBox(BlockPos bp, double height, float width, ColorSetting color) {
        drawBoundingBox(getBoundingBox(bp,1, height,1), width, color, color.getAlpha());
    }

    public static void drawBoundingBox(AxisAlignedBB bb, double width, ColorSetting color) {
        drawBoundingBox(bb, width, color, color.getAlpha());
    }

    public static void drawBoundingBox(AxisAlignedBB bb, double width, ColorSetting color, int alpha) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.glLineWidth((float) width);
        color.getColor();
        bufferbuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        colorVertex(bb.minX, bb.minY, bb.minZ,color,color.getAlpha(), bufferbuilder);
        colorVertex(bb.minX, bb.minY, bb.maxZ,color,color.getAlpha(), bufferbuilder);
        colorVertex(bb.maxX, bb.minY, bb.maxZ,color,color.getAlpha(), bufferbuilder);
        colorVertex(bb.maxX, bb.minY, bb.minZ,color,color.getAlpha(), bufferbuilder);
        colorVertex(bb.minX, bb.minY, bb.minZ,color,color.getAlpha(), bufferbuilder);
        colorVertex(bb.minX, bb.maxY, bb.minZ,color, alpha, bufferbuilder);
        colorVertex(bb.minX, bb.maxY, bb.maxZ,color, alpha, bufferbuilder);
        colorVertex(bb.minX, bb.minY, bb.maxZ,color,color.getAlpha(), bufferbuilder);
        colorVertex(bb.maxX, bb.minY, bb.maxZ,color,color.getAlpha(), bufferbuilder);
        colorVertex(bb.maxX, bb.maxY, bb.maxZ,color, alpha, bufferbuilder);
        colorVertex(bb.minX, bb.maxY, bb.maxZ,color, alpha, bufferbuilder);
        colorVertex(bb.maxX, bb.maxY, bb.maxZ,color, alpha, bufferbuilder);
        colorVertex(bb.maxX, bb.maxY, bb.minZ,color, alpha, bufferbuilder);
        colorVertex(bb.maxX, bb.minY, bb.minZ,color,color.getAlpha(), bufferbuilder);
        colorVertex(bb.maxX, bb.maxY, bb.minZ,color, alpha, bufferbuilder);
        colorVertex(bb.minX, bb.maxY, bb.minZ,color, alpha, bufferbuilder);
        tessellator.draw();
    }

    
    public static void drawBoundingBoxWithSides(BlockPos blockPos, int width, ColorSetting color, int sides) {
        drawBoundingBoxWithSides(getBoundingBox(blockPos, 1, 1, 1), width, color, color.getAlpha(), sides);
    }

    public static void drawBoundingBoxWithSides(BlockPos blockPos, int width, ColorSetting color, int alpha, int sides) {
        drawBoundingBoxWithSides(getBoundingBox(blockPos, 1, 1, 1), width, color, alpha, sides);
    }

    public static void drawBoundingBoxWithSides(AxisAlignedBB axisAlignedBB, int width, ColorSetting color, int sides) {
        drawBoundingBoxWithSides(axisAlignedBB,width,color,color.getAlpha(),sides);
    }

    public static void drawBoundingBoxWithSides(AxisAlignedBB axisAlignedBB, int width, ColorSetting color, int alpha, int sides) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.glLineWidth(width);
        bufferbuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        doVerticies(axisAlignedBB,color, alpha, bufferbuilder,sides,true);
        tessellator.draw();
    }

    
    private static class Points {
        
        double[][] point = new double[10][2];
        
        private int count = 0;
        
        private final double xCenter;
        private final double zCenter;
        
        public final double yMin;
        public final double yMax;
        
        private final float rotation;
        
        public Points(double yMin, double yMax, double xCenter, double zCenter, float rotation) {
            this.yMin = yMin;
            this.yMax = yMax;
            this.xCenter = xCenter;
            this.zCenter = zCenter;
            this.rotation = rotation;
        }
        
        public void addPoints(double x, double z) {
            
            
            x -= xCenter;
            z -= zCenter;
            
            double rotateX = x * Math.cos(rotation) - z * Math.sin(rotation);
            double rotateZ = x * Math.sin(rotation) + z * Math.cos(rotation);
            
            rotateX += xCenter;
            rotateZ += zCenter;
            
            point[count++] = new double[] {rotateX, rotateZ};
        }
        
        public double[] getPoint(int index) {
            return point[index];
        }

    }

    public static void drawBox(BlockPos blockPos, double height, ColorSetting color, int sides) {
        drawBox(blockPos.getX(), blockPos.getY(), blockPos.getZ(), 1, height, 1, color, color.getAlpha(), sides);
    }

    public static void drawBox(AxisAlignedBB bb, ColorSetting color) {
        drawBox(bb, true, 1, color, color.getAlpha(), FaceMasks.Quad.ALL);
    }

    public static void drawBox(AxisAlignedBB bb, double height, ColorSetting color, int sides) {
        drawBox(bb, false, height, color, color.getAlpha(), sides);
    }

    public static void drawBox(AxisAlignedBB bb, boolean check, double height, ColorSetting color, int sides) {
        drawBox(bb,check,height,color,color.getAlpha(),sides);
    }

    public static void drawBox(AxisAlignedBB bb, boolean check, double height, ColorSetting color, int alpha, int sides) {
        if (check) {
            drawBox(bb.minX, bb.minY, bb.minZ, bb.maxX-bb.minX, bb.maxY-bb.minY, bb.maxZ-bb.minZ, color, alpha, sides);
        }
        else {
            drawBox(bb.minX, bb.minY, bb.minZ, bb.maxX-bb.minX, height, bb.maxZ-bb.minZ, color, alpha, sides);
        }
    }

    public static void drawBox(double x, double y, double z, double w, double h, double d, ColorSetting color, int alpha, int sides) {
        GlStateManager.disableAlpha();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        color.getColor();
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        doVerticies(new AxisAlignedBB(x,y,z,x+w,y+h,z+d),color, alpha, bufferbuilder,sides,false);
        tessellator.draw();
        GlStateManager.enableAlpha();
    }

    public static void drawLine(double posx, double posy, double posz, double posx2, double posy2, double posz2, ColorSetting color) {
        drawLine(posx,posy,posz,posx2,posy2,posz2,color,1);
    }

    public static void drawLine(double posx, double posy, double posz, double posx2, double posy2, double posz2, ColorSetting color, float width) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.glLineWidth(width);
        color.getColor();
        bufferbuilder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        vertex(posx,posy,posz, bufferbuilder);
        vertex(posx2,posy2,posz2, bufferbuilder);
        tessellator.draw();
    }
    
    public static void drawDirection(Points square, ColorSetting color, float width) {
        for(int i = 0; i < 4; i++) {
            drawLine(square.getPoint(i)[0], square.yMin, square.getPoint(i)[1],
                    square.getPoint((i + 1) % 4)[0], square.yMin, square.getPoint((i + 1) % 4)[1],
                    color, width
            );
        }
        
        for(int i = 0; i < 4; i++) {
            drawLine(square.getPoint(i)[0], square.yMax, square.getPoint(i)[1],
                    square.getPoint((i + 1) % 4)[0], square.yMax, square.getPoint((i + 1) % 4)[1],
                    color, width
            );
        }
        
        for(int i = 0; i < 4; i++) {
            drawLine(square.getPoint(i)[0], square.yMin, square.getPoint(i)[1],
                    square.getPoint(i)[0], square.yMax, square.getPoint(i)[1],
                    color, width
            );
        }
    }

    private static void vertex(double x, double y, double z, BufferBuilder bufferbuilder) {
        bufferbuilder.pos(x-mc.getRenderManager().viewerPosX,y-mc.getRenderManager().viewerPosY,z-mc.getRenderManager().viewerPosZ).endVertex();
    }

    private static void colorVertex(double x, double y, double z, ColorSetting color, int alpha, BufferBuilder bufferbuilder) {
        bufferbuilder.pos(x-mc.getRenderManager().viewerPosX,y-mc.getRenderManager().viewerPosY,z-mc.getRenderManager().viewerPosZ).color(color.getRed(), color.getGreen(), color.getBlue(), alpha).endVertex();
    }

    private static AxisAlignedBB getBoundingBox(BlockPos bp, double width, double height, double depth) {
        double x=bp.getX();
        double y=bp.getY();
        double z=bp.getZ();
        return new AxisAlignedBB(x,y,z,x+width,y+height,z+depth);
    }

    private static void doVerticies(AxisAlignedBB axisAlignedBB, ColorSetting color, int alpha, BufferBuilder bufferbuilder, int sides, boolean five) {
        if ((sides & FaceMasks.Quad.EAST) != 0) {
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ,color,color.getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ,color,color.getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ,color, alpha, bufferbuilder);
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ,color, alpha, bufferbuilder);
            if (five) colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ,color,color.getAlpha(), bufferbuilder);
        }
        if ((sides & FaceMasks.Quad.WEST) != 0) {
            colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ,color,color.getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ,color,color.getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ,color, alpha, bufferbuilder);
            colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ,color, alpha, bufferbuilder);
            if (five) colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ,color,color.getAlpha(), bufferbuilder);
        }
        if ((sides & FaceMasks.Quad.NORTH) != 0) {
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ,color,color.getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ,color,color.getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ,color, alpha, bufferbuilder);
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ,color, alpha, bufferbuilder);
            if (five) colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ,color,color.getAlpha(), bufferbuilder);
        }
        if ((sides & FaceMasks.Quad.SOUTH) != 0) {
            colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ,color,color.getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ,color,color.getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ,color, alpha, bufferbuilder);
            colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ,color, alpha, bufferbuilder);
            if (five) colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ,color,color.getAlpha(), bufferbuilder);
        }
        if ((sides & FaceMasks.Quad.UP) != 0) {
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ,color, alpha, bufferbuilder);
            colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ,color, alpha, bufferbuilder);
            colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ,color, alpha, bufferbuilder);
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ,color, alpha, bufferbuilder);
            if (five) colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ,color, alpha, bufferbuilder);
        }
        if ((sides & FaceMasks.Quad.DOWN) != 0) {
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ,color,color.getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ,color,color.getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ,color,color.getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ,color,color.getAlpha(), bufferbuilder);
            if (five) colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ,color,color.getAlpha(), bufferbuilder);
        }
    }
    
    public static void prepare() {
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ZERO, GL11.GL_ONE);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableAlpha();
        glEnable(GL11.GL_LINE_SMOOTH);
        glEnable(GL32.GL_DEPTH_CLAMP);
    }

    public static void release() {
        GL11.glDisable(GL32.GL_DEPTH_CLAMP);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.enableAlpha();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.glLineWidth(1.0f);
        GlStateManager.shadeModel(GL11.GL_FLAT);
        glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_DONT_CARE);
        GL11.glPopAttrib();
    }
}
