package me.darki.konas.util.render;

import me.darki.konas.mixin.mixins.IRenderManager;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.EXTPackedDepthStencil;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.glu.Sphere;

import java.awt.*;
import java.util.Collection;
import java.util.Collections;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author (not) cats
 * This is pasted together from a few different spots
 */
public class RenderUtil {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private static final Frustum frustrum = new Frustum();

    public static void drawLine(Vec3d startPos, Vec3d endPos, Color color) {

        double posX = ((IRenderManager) mc.getRenderManager()).getRenderPosX();
        double posY = ((IRenderManager) mc.getRenderManager()).getRenderPosY();
        double posZ = ((IRenderManager) mc.getRenderManager()).getRenderPosZ();

        final Vec3d modifiedStartPos = startPos.add(-posX, -posY, -posZ);
        final Vec3d modifiedEndPos = endPos.add(-posX, -posY, -posZ);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(modifiedStartPos.x, modifiedStartPos.y, modifiedStartPos.z).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        buffer.pos(modifiedEndPos.x, modifiedEndPos.y, modifiedEndPos.z).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        tessellator.draw();
    }

    /**
     *
     */
    public static void circleESP(double posX, double posY, double posZ, double radius, Color color) {
        double x = posX - ((IRenderManager) mc.getRenderManager()).getRenderPosX();
        double y = posY - ((IRenderManager) mc.getRenderManager()).getRenderPosY();
        double z = posZ - ((IRenderManager) mc.getRenderManager()).getRenderPosZ();
        glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        glBegin(1);
        for (int i = 0; i <= 360; ++i) {
            glVertex3d(x + Math.sin(i * Math.PI / 180.0) * radius, y, z + Math.cos(i * Math.PI / 180.0) * radius);
        }
        glEnd();
    }

    public static void drawTracerPointer(float x, float y, float size, float widthDiv, float heightDiv, boolean outline, float outlineWidth, int color) {
        boolean blend = GL11.glIsEnabled(GL_BLEND);
        float alpha = (color >> 24 & 0xFF) / 255.0F;
        GL11.glEnable(GL_BLEND);
        GL11.glDisable(GL_TEXTURE_2D);
        GL11.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL_LINE_SMOOTH);
        GL11.glPushMatrix();
        hexColor(color);
        GL11.glBegin(7);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d((x - size / widthDiv), (y + size));
        GL11.glVertex2d(x, (y + size / heightDiv));
        GL11.glVertex2d((x + size / widthDiv), (y + size));
        GL11.glVertex2d(x, y);
        GL11.glEnd();
        if (outline) {
            GL11.glLineWidth(outlineWidth);
            GL11.glColor4f(0.0F, 0.0F, 0.0F, alpha);
            GL11.glBegin(2);
            GL11.glVertex2d(x, y);
            GL11.glVertex2d((x - size / widthDiv), (y + size));
            GL11.glVertex2d(x, (y + size / heightDiv));
            GL11.glVertex2d((x + size / widthDiv), (y + size));
            GL11.glVertex2d(x, y);
            GL11.glEnd();
        }
        GL11.glPopMatrix();
        GL11.glEnable(GL_TEXTURE_2D);
        if (!blend)
            GL11.glDisable(GL_BLEND);
        GL11.glDisable(GL_LINE_SMOOTH);
    }

    public static void drawSphere(double x, double y, double z, float size, int slices, int stacks) {
        Sphere s = new Sphere();
        GL11.glPushMatrix();
        GL11.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL_BLEND);
        GL11.glLineWidth(1.2F);
        GL11.glDisable(GL_TEXTURE_2D);
        GL11.glDisable(GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        s.setDrawStyle(GLU.GLU_SILHOUETTE);
        double renderX = ((IRenderManager) mc.getRenderManager()).getRenderPosX();
        double renderY = ((IRenderManager) mc.getRenderManager()).getRenderPosY();
        double renderZ = ((IRenderManager) mc.getRenderManager()).getRenderPosZ();
        GL11.glTranslated(x - renderX, y - renderY, z - renderZ);
        s.draw(size, slices, stacks);
        GL11.glLineWidth(2.0F);
        GL11.glEnable(GL_TEXTURE_2D);
        GL11.glEnable(GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glDisable(GL_BLEND);
        GL11.glPopMatrix();
    }

    public static void hexColor(int hexColor) {
        float red = (hexColor >> 16 & 0xFF) / 255.0F;
        float green = (hexColor >> 8 & 0xFF) / 255.0F;
        float blue = (hexColor & 0xFF) / 255.0F;
        float alpha = (hexColor >> 24 & 0xFF) / 255.0F;
        GL11.glColor4f(red, green, blue, alpha);
    }

    public static void vertexBB(AxisAlignedBB axisalignedbb, float red, float green, float blue, float alpha) {
        Tessellator ts = Tessellator.getInstance();
        BufferBuilder vb = ts.getBuffer();
        vb.begin(7, DefaultVertexFormats.POSITION_TEX);// Starts X.
        vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        ts.draw();
        vb.begin(7, DefaultVertexFormats.POSITION_TEX);
        vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        ts.draw();// Ends X.
        vb.begin(7, DefaultVertexFormats.POSITION_TEX);// Starts Y.
        vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        ts.draw();
        vb.begin(7, DefaultVertexFormats.POSITION_TEX);
        vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        ts.draw();// Ends Y.
        vb.begin(7, DefaultVertexFormats.POSITION_TEX);// Starts Z.
        vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        ts.draw();
        vb.begin(7, DefaultVertexFormats.POSITION_TEX);
        vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        ts.draw();// Ends Z.
    }

    public static boolean isInViewFrustrum(Entity entity) {
        return (isInViewFrustrum(entity.getEntityBoundingBox()) || entity.ignoreFrustumCheck);
    }

    public static boolean isInViewFrustrum(AxisAlignedBB bb) {
        Entity current = Minecraft.getMinecraft().getRenderViewEntity();
        frustrum.setPosition(current.posX, current.posY, current.posZ);
        return frustrum.isBoundingBoxInFrustum(bb);
    }

    public static void blockEsp(Collection<BlockPos> blockPoses, float red, float green, float blue, float alpha, double length, double length2) {
        beginRender();
        GlStateManager.disableTexture2D();
        GlStateManager.disableAlpha();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);

        double renderX = ((IRenderManager) mc.getRenderManager()).getRenderPosX();
        double renderY = ((IRenderManager) mc.getRenderManager()).getRenderPosY();
        double renderZ = ((IRenderManager) mc.getRenderManager()).getRenderPosZ();
        glColor4d(red, green, blue, alpha);

        for (BlockPos blockPos : blockPoses) {
            GlStateManager.pushMatrix();

            double x = blockPos.getX() - renderX;
            double y = blockPos.getY() - renderY;
            double z = blockPos.getZ() - renderZ;

            AxisAlignedBB axisalignedbb = new AxisAlignedBB(x, y, z, x + length2, y + 1.0, z + length);
            vertexBB(axisalignedbb, red, green, blue, alpha);

            GlStateManager.popMatrix();
        }

        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableAlpha();
        endRender();
    }

    public static void blockEsp(BlockPos blockPos, int c, double length, double length2) {
        float red = (float) (c >> 16 & 255) / 255.0F;
        float green = (float) (c >> 8 & 255) / 255.0F;
        float blue = (float) (c & 255) / 255.0F;
        float alpha = (float) (c >> 24 & 255) / 255.0F;

        blockEsp(Collections.singleton(blockPos), red, green, blue, alpha, length, length2);
    }

    public static void drawSelectionBoundingBox(RayTraceResult result, int c, float partialTicks) {
        if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
            float red = (float) (c >> 16 & 255) / 255.0F;
            float green = (float) (c >> 8 & 255) / 255.0F;
            float blue = (float) (c & 255) / 255.0F;
            float alpha = (float) (c >> 24 & 255) / 255.0F;

            RenderUtil.beginRender();
            GlStateManager.glLineWidth(2.0F);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            BlockPos blockpos = result.getBlockPos();
            IBlockState iblockstate = mc.world.getBlockState(blockpos);

            if (iblockstate.getMaterial() != Material.AIR && mc.world.getWorldBorder().contains(blockpos)) {
                double x = mc.player.lastTickPosX + (mc.player.posX - mc.player.lastTickPosX) * partialTicks;
                double y = mc.player.lastTickPosY + (mc.player.posY - mc.player.lastTickPosY) * partialTicks;
                double z = mc.player.lastTickPosZ + (mc.player.posZ - mc.player.lastTickPosZ) * partialTicks;
                drawBoxOutline(iblockstate.getSelectedBoundingBox(mc.world, blockpos).grow(0.0020000000949949026D).offset(-x, -y, -z), red, green, blue, alpha);
            }

            RenderUtil.endRender();
            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
        }
    }

    public static void drawEntityBoundingBox(Entity entity, int c, float partialTicks) {

        final IRenderManager renderManager = (IRenderManager) mc.getRenderManager();

        final double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks
                - renderManager.getRenderPosX();
        final double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks
                - renderManager.getRenderPosY();
        final double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks
                - renderManager.getRenderPosZ();

        final AxisAlignedBB entityBox = entity.getEntityBoundingBox();
        final AxisAlignedBB axisAlignedBB = new AxisAlignedBB(
                entityBox.minX - entity.posX + x,
                entityBox.minY - entity.posY + y,
                entityBox.minZ - entity.posZ + z,
                entityBox.maxX - entity.posX + x,
                entityBox.maxY - entity.posY + y,
                entityBox.maxZ - entity.posZ + z
        );

        if (entity != mc.player) {
            float[] rgb = ColorUtils.intToRGB(c);
            drawBoxOutline(axisAlignedBB.grow(0.0020000000949949026D), rgb[0], rgb[1], rgb[2], rgb[3]);
        }
    }


    public static void drawBox(AxisAlignedBB box, float red, float green, float blue, float alpha) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
    }

    public static void drawBoxEdges(AxisAlignedBB box, float red, float green, float blue, float alpha) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(box.minX, box.minY, box.minZ).color(red, green, blue, 0.0F).endVertex();
        buffer.pos(box.minX, box.minY, box.maxZ - 0.8 * (box.maxZ - box.minZ)).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.minX, box.minY, box.maxZ).color(red, green, blue, 0.0F).endVertex();
        buffer.pos(box.minX, box.minY, box.minZ + 0.8 * (box.maxZ - box.minZ)).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.maxX, box.minY, box.minZ).color(red, green, blue, 0.0F).endVertex();
        buffer.pos(box.maxX, box.minY, box.maxZ - 0.8 * (box.maxZ - box.minZ)).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.maxX, box.minY, box.maxZ).color(red, green, blue, 0.0F).endVertex();
        buffer.pos(box.maxX, box.minY, box.minZ + 0.8 * (box.maxZ - box.minZ)).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.minX, box.minY, box.minZ).color(red, green, blue, 0.0F).endVertex();
        buffer.pos(box.maxX - 0.8 * (box.maxX - box.minX), box.minY, box.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.minX, box.minY, box.maxZ).color(red, green, blue, 0.0F).endVertex();
        buffer.pos(box.maxX - 0.8 * (box.maxX - box.minX), box.minY, box.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.maxX, box.minY, box.minZ).color(red, green, blue, 0.0F).endVertex();
        buffer.pos(box.minX + 0.8 * (box.maxX - box.minX), box.minY, box.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.maxX, box.minY, box.maxZ).color(red, green, blue, 0.0F).endVertex();
        buffer.pos(box.minX + 0.8 * (box.maxX - box.minX), box.minY, box.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.minX, box.minY, box.minZ).color(red, green, blue, 0.0F).endVertex();
        buffer.pos(box.minX, box.minY + 0.2 * (box.maxY - box.minY), box.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.minX, box.minY, box.maxZ).color(red, green, blue, 0.0F).endVertex();
        buffer.pos(box.minX, box.minY + 0.2 * (box.maxY - box.minY), box.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.maxX, box.minY, box.minZ).color(red, green, blue, 0.0F).endVertex();
        buffer.pos(box.maxX, box.minY + 0.2 * (box.maxY - box.minY), box.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.maxX, box.minY, box.maxZ).color(red, green, blue, 0.0F).endVertex();
        buffer.pos(box.maxX, box.minY + 0.2 * (box.maxY - box.minY), box.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.minX, box.maxY, box.minZ).color(red, green, blue, 0.0F).endVertex();
        buffer.pos(box.minX, box.maxY, box.maxZ - 0.8 * (box.maxZ - box.minZ)).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.minX, box.maxY, box.maxZ).color(red, green, blue, 0.0F).endVertex();
        buffer.pos(box.minX, box.maxY, box.minZ + 0.8 * (box.maxZ - box.minZ)).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.maxX, box.maxY, box.minZ).color(red, green, blue, 0.0F).endVertex();
        buffer.pos(box.maxX, box.maxY, box.maxZ - 0.8 * (box.maxZ - box.minZ)).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.maxX, box.maxY, box.maxZ).color(red, green, blue, 0.0F).endVertex();
        buffer.pos(box.maxX, box.maxY, box.minZ + 0.8 * (box.maxZ - box.minZ)).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.minX, box.maxY, box.minZ).color(red, green, blue, 0.0F).endVertex();
        buffer.pos(box.maxX - 0.8 * (box.maxX - box.minX), box.maxY, box.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.minX, box.maxY, box.maxZ).color(red, green, blue, 0.0F).endVertex();
        buffer.pos(box.maxX - 0.8 * (box.maxX - box.minX), box.maxY, box.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.maxX, box.maxY, box.minZ).color(red, green, blue, 0.0F).endVertex();
        buffer.pos(box.minX + 0.8 * (box.maxX - box.minX), box.maxY, box.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.maxX, box.maxY, box.maxZ).color(red, green, blue, 0.0F).endVertex();
        buffer.pos(box.minX + 0.8 * (box.maxX - box.minX), box.maxY, box.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.minX, box.maxY, box.minZ).color(red, green, blue, 0.0F).endVertex();
        buffer.pos(box.minX, box.maxY - 0.2 * (box.maxY - box.minY), box.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.minX, box.maxY, box.maxZ).color(red, green, blue, 0.0F).endVertex();
        buffer.pos(box.minX, box.maxY - 0.2 * (box.maxY - box.minY), box.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.maxX, box.maxY, box.minZ).color(red, green, blue, 0.0F).endVertex();
        buffer.pos(box.maxX, box.maxY - 0.2 * (box.maxY - box.minY), box.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.maxX, box.maxY, box.maxZ).color(red, green, blue, 0.0F).endVertex();
        buffer.pos(box.maxX, box.maxY - 0.2 * (box.maxY - box.minY), box.maxZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
    }

    public static void drawBoxOutline(AxisAlignedBB box, float red, float green, float blue, float alpha) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
        //drawBoundingBox(bufferbuilder, minX, minY, minZ, maxX, maxY, maxZ, red, green, blue, alpha);
        buffer.pos(box.minX, box.minY, box.minZ).color(red, green, blue, 0.0F).endVertex();
        buffer.pos(box.minX, box.minY, box.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.maxX, box.minY, box.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.maxX, box.minY, box.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.minX, box.minY, box.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.minX, box.minY, box.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.minX, box.maxY, box.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.maxX, box.maxY, box.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.maxX, box.maxY, box.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.minX, box.maxY, box.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.minX, box.maxY, box.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.minX, box.maxY, box.maxZ).color(red, green, blue, 0.0F).endVertex();
        buffer.pos(box.minX, box.minY, box.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.maxX, box.maxY, box.maxZ).color(red, green, blue, 0.0F).endVertex();
        buffer.pos(box.maxX, box.minY, box.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.maxX, box.maxY, box.minZ).color(red, green, blue, 0.0F).endVertex();
        buffer.pos(box.maxX, box.minY, box.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.maxX, box.minY, box.minZ).color(red, green, blue, 0.0F).endVertex();
        tessellator.draw();
    }

    public static void drawBoundingBox(final AxisAlignedBB bb, Color color) {
        AxisAlignedBB boundingBox = bb.offset(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ);

        drawBoxOutline(boundingBox.grow(0.0020000000949949026D), color.getRed() * 255, color.getGreen() * 255, color.getBlue() * 255, color.getAlpha() * 255);
    }

    public static void drawBoundingBox(final AxisAlignedBB bb, int color) {
        drawBoundingBox(bb, new Color(color));
    }

    public static void drawBoundingEdgesBox(final AxisAlignedBB bb, Color color) {
        AxisAlignedBB boundingBox = bb.offset(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ);

        drawBoxEdges(boundingBox.grow(0.0020000000949949026D), color.getRed() * 255, color.getGreen() * 255, color.getBlue() * 255, color.getAlpha() * 255);
    }

    public static void drawBoundingEdgesBox(final AxisAlignedBB bb, int color) {
        drawBoundingEdgesBox(bb, new Color(color));
    }

    public static void drawFilledBoundingBox(final AxisAlignedBB bb, Color color) {
        AxisAlignedBB boundingBox = bb.offset(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ);

        glColor(color);

        drawFilledBox(boundingBox);
    }

    public static void drawFilledBoundingBox(final AxisAlignedBB bb, int color) {
        AxisAlignedBB boundingBox = bb.offset(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ);

        glColor(color);

        drawFilledBox(boundingBox);
    }

    public static void drawEntityBox(final Entity entity, final int color, double partialTicks) {
        final IRenderManager renderManager = (IRenderManager) mc.getRenderManager();

        final double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks
                - renderManager.getRenderPosX();
        final double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks
                - renderManager.getRenderPosY();
        final double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks
                - renderManager.getRenderPosZ();

        final AxisAlignedBB entityBox = entity.getEntityBoundingBox();
        final AxisAlignedBB axisAlignedBB = new AxisAlignedBB(
                entityBox.minX - entity.posX + x,
                entityBox.minY - entity.posY + y,
                entityBox.minZ - entity.posZ + z,
                entityBox.maxX - entity.posX + x,
                entityBox.maxY - entity.posY + y,
                entityBox.maxZ - entity.posZ + z
        );

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        float[] rgb = ColorUtils.intToRGB(color);
        glColor4f(rgb[0], rgb[1], rgb[2], rgb[3]);
        drawFilledBox(axisAlignedBB);
    }

    public static void glColor(final Color color) {
        final float red = color.getRed() / 255F;
        final float green = color.getGreen() / 255F;
        final float blue = color.getBlue() / 255F;
        final float alpha = color.getAlpha() / 255F;

        GlStateManager.color(red, green, blue, alpha);
    }

    private static void glColor(final int hex) {
        final float alpha = (hex >> 24 & 0xFF) / 255F;
        final float red = (hex >> 16 & 0xFF) / 255F;
        final float green = (hex >> 8 & 0xFF) / 255F;
        final float blue = (hex & 0xFF) / 255F;

        GlStateManager.color(red, green, blue, alpha);
    }

    public static void drawFilledBox(final AxisAlignedBB axisAlignedBB) {
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder worldRenderer = tessellator.getBuffer();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        tessellator.draw();
    }

    public static float getNametagSize(EntityLivingBase player) {
        return getNametagSize(player.posX, player.posY, player.posZ);
    }

    public static float getNametagSize(double x, double y, double z) {
        ScaledResolution scaledRes = new ScaledResolution(mc);
        double twoDscale = scaledRes.getScaleFactor() / Math.pow(scaledRes.getScaleFactor(), 2.0D);
        return (float) twoDscale + ((float) mc.player.getDistance(x, y, z) / (0.7f * 10));
    }

    //Note: use these functions for render modules, the idea is to mainly catch most mistakes that could cause render issues

    /**
     * Prepares for the use of GL, resetting color and other things, hopefully fixing the grey box issue
     * Idea came from Kami's prepareGL()
     */
    public static void beginRender() {
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        //GlStateManager.color(1, 1, 1, 1);
    }

    /**
     * ends the use of GL, to make sure that everything was reset properly
     * Also came from Kami
     */
    public static void endRender() {
        //GlStateManager.resetColor();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
    }


    /**
     * Author: Hexeption
     */

    public static class OutlineUtils {

        public static void renderOne(int width) {
            checkSetupFBO();
            glPushAttrib(GL_ALL_ATTRIB_BITS);
            glDisable(GL_ALPHA_TEST);
            glDisable(GL_TEXTURE_2D);
            glDisable(GL_LIGHTING);
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glLineWidth(width);
            glEnable(GL_LINE_SMOOTH);
            glEnable(GL_STENCIL_TEST);
            glClear(GL_STENCIL_BUFFER_BIT);
            glClearStencil(0xF);
            glStencilFunc(GL_NEVER, 1, 0xF);
            glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE);
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        }

        public static void renderTwo() {
            glStencilFunc(GL_NEVER, 0, 0xF);
            glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE);
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        }

        public static void renderThree() {
            glStencilFunc(GL_EQUAL, 1, 0xF);
            glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        }

        public static void renderFour(int color, float offset) {
            setColor(new Color(color));
            glDepthMask(false);
            glDisable(GL_DEPTH_TEST);
            glEnable(GL_POLYGON_OFFSET_LINE);
            glPolygonOffset(offset, -2000000F);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
        }

        public static void renderFive(float offset) {
            glPolygonOffset(-offset, 2000000F);
            glDisable(GL_POLYGON_OFFSET_LINE);
            glEnable(GL_DEPTH_TEST);
            glDepthMask(true);
            glDisable(GL_STENCIL_TEST);
            glDisable(GL_LINE_SMOOTH);
            glHint(GL_LINE_SMOOTH_HINT, GL_DONT_CARE);
            glEnable(GL_BLEND);
            glEnable(GL_LIGHTING);
            glEnable(GL_TEXTURE_2D);
            glEnable(GL_ALPHA_TEST);
            glPopAttrib();
        }

        public static void setColor(Color c) {
            glColor4d(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f);
        }

        public static void checkSetupFBO() {
            Framebuffer fbo = mc.getFramebuffer();
            if (fbo.depthBuffer > -1) {
                setupFBO(fbo);
                fbo.depthBuffer = -1;
            }
        }

        /**
         * Sets up the FBO with depth and stencil
         *
         * @param fbo Framebuffer
         */
        public static void setupFBO(Framebuffer fbo) {
            // Deletes old render buffer extensions such as depth
            // Args: Render Buffer ID
            EXTFramebufferObject.glDeleteRenderbuffersEXT(fbo.depthBuffer);
            // Generates a new render buffer ID for the depth and stencil extension
            int stencil_depth_buffer_ID = EXTFramebufferObject.glGenRenderbuffersEXT();
            // Binds new render buffer by ID
            // Args: Target (GL_RENDERBUFFER_EXT), ID
            EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencil_depth_buffer_ID);
            // Adds the depth and stencil extension
            // Args: Target (GL_RENDERBUFFER_EXT), Extension (GL_DEPTH_STENCIL_EXT),
            // Width, Height
            EXTFramebufferObject.glRenderbufferStorageEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, EXTPackedDepthStencil.GL_DEPTH_STENCIL_EXT, mc.displayWidth, mc.displayHeight);
            // Adds the stencil attachment
            // Args: Target (GL_FRAMEBUFFER_EXT), Attachment
            // (GL_STENCIL_ATTACHMENT_EXT), Target (GL_RENDERBUFFER_EXT), ID
            EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_STENCIL_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencil_depth_buffer_ID);
            // Adds the depth attachment
            // Args: Target (GL_FRAMEBUFFER_EXT), Attachment
            // (GL_DEPTH_ATTACHMENT_EXT), Target (GL_RENDERBUFFER_EXT), ID
            EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencil_depth_buffer_ID);
        }

    }

    // only bad skid render methods above
    // strong swiss render methods below
    public static void draw1DGradientRect(float left, float top, float right, float bottom, int leftColor, int rightColor) {
        float la = (float) (leftColor >> 24 & 255) / 255.0F;
        float lr = (float) (leftColor >> 16 & 255) / 255.0F;
        float lg = (float) (leftColor >> 8 & 255) / 255.0F;
        float lb = (float) (leftColor & 255) / 255.0F;
        float ra = (float) (rightColor >> 24 & 255) / 255.0F;
        float rr = (float) (rightColor >> 16 & 255) / 255.0F;
        float rg = (float) (rightColor >> 8 & 255) / 255.0F;
        float rb = (float) (rightColor & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(right, top, 0).color(rr, rg, rb, ra).endVertex();
        bufferbuilder.pos(left, top, 0).color(lr, lg, lb, la).endVertex();
        bufferbuilder.pos(left, bottom, 0).color(lr, lg, lb, la).endVertex();
        bufferbuilder.pos(right, bottom, 0).color(rr, rg, rb, ra).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void draw2DGradientRect(float left, float top, float right, float bottom, int leftBottomColor, int leftTopColor, int rightBottomColor, int rightTopColor) {
        float lba = (float) (leftBottomColor >> 24 & 255) / 255.0F;
        float lbr = (float) (leftBottomColor >> 16 & 255) / 255.0F;
        float lbg = (float) (leftBottomColor >> 8 & 255) / 255.0F;
        float lbb = (float) (leftBottomColor & 255) / 255.0F;
        float rba = (float) (rightBottomColor >> 24 & 255) / 255.0F;
        float rbr = (float) (rightBottomColor >> 16 & 255) / 255.0F;
        float rbg = (float) (rightBottomColor >> 8 & 255) / 255.0F;
        float rbb = (float) (rightBottomColor & 255) / 255.0F;
        float lta = (float) (leftTopColor >> 24 & 255) / 255.0F;
        float ltr = (float) (leftTopColor >> 16 & 255) / 255.0F;
        float ltg = (float) (leftTopColor >> 8 & 255) / 255.0F;
        float ltb = (float) (leftTopColor & 255) / 255.0F;
        float rta = (float) (rightTopColor >> 24 & 255) / 255.0F;
        float rtr = (float) (rightTopColor >> 16 & 255) / 255.0F;
        float rtg = (float) (rightTopColor >> 8 & 255) / 255.0F;
        float rtb = (float) (rightTopColor & 255) / 255.0F;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(right, top, 0).color(rtr, rtg, rtb, rta).endVertex();
        bufferbuilder.pos(left, top, 0).color(ltr, ltg, ltb, lta).endVertex();
        bufferbuilder.pos(left, bottom, 0).color(lbr, lbg, lbb, lba).endVertex();
        bufferbuilder.pos(right, bottom, 0).color(rbr, rbg, rbb, rba).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawOutlineRect(int left, int top, int right, int bottom, float lineWidth, int color) {

        if (left < right)
        {
            int i = left;
            left = right;
            right = i;
        }

        if (top < bottom)
        {
            int j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(f, f1, f2, f3);
        GL11.glEnable(GL_LINE_SMOOTH);
        GlStateManager.glLineWidth(lineWidth);
        bufferbuilder.begin(GL_LINES, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(left, top, 0.0D). endVertex();
        bufferbuilder.pos(right, top, 0.0D).endVertex();
        bufferbuilder.pos(right, top, 0.0D).endVertex();
        bufferbuilder.pos(right, bottom, 0.0D).endVertex();
        bufferbuilder.pos(right, bottom, 0.0D).endVertex();
        bufferbuilder.pos(left, bottom, 0.0D).endVertex();
        bufferbuilder.pos(left, bottom, 0.0D).endVertex();
        bufferbuilder.pos(left, top, 0.0D). endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GL11.glDisable(GL_LINE_SMOOTH);
    }

}
