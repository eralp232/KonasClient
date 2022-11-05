package me.darki.konas.util.render;

import me.darki.konas.mixin.mixins.IRenderManager;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import static org.lwjgl.opengl.GL11.*;


/**
 * @author cats
 * Basic render utilities for ESP, planning to change and revise this later, right now it's just the one I used for Vial
 */
public class RenderUtils {

    private static final Minecraft mc = Minecraft.getMinecraft();

    /**
     * Draws a defined bounding box
     *
     * @param box   the bounding box to draw
     * @param red   red shade
     * @param green green shade
     * @param blue  blue shade
     * @param alpha alpha value, or opacity
     */
    public static void drawBoundingBox(AxisAlignedBB box, Float red, Float green, Float blue, Float alpha) {
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        //drawBoundingBox(bufferbuilder, minX, minY, minZ, maxX, maxY, maxZ, red, green, blue, alpha);
        buffer.pos(box.minX, box.minY, box.minZ).color(red, green, blue, 0.0f).endVertex();
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
        buffer.pos(box.minX, box.maxY, box.maxZ).color(red, green, blue, 0.0f).endVertex();
        buffer.pos(box.minX, box.minY, box.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.maxX, box.maxY, box.maxZ).color(red, green, blue, 0.0f).endVertex();
        buffer.pos(box.maxX, box.minY, box.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.maxX, box.maxY, box.minZ).color(red, green, blue, 0.0f).endVertex();
        buffer.pos(box.maxX, box.minY, box.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.maxX, box.minY, box.minZ).color(red, green, blue, 0.0f).endVertex();
        tessellator.draw();
    }

    public static void drawBox(AxisAlignedBB box, int mode, int color) {
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(mode, DefaultVertexFormats.POSITION_COLOR);
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        float a = (float) (color >> 24 & 255) / 255.0F;
        buffer.pos(box.minX, box.minY, box.minZ).color(r, g, b, 0.0f).endVertex();
        buffer.pos(box.minX, box.minY, box.minZ).color(r, g, b, a).endVertex();
        buffer.pos(box.maxX, box.minY, box.minZ).color(r, g, b, a).endVertex();
        buffer.pos(box.maxX, box.minY, box.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(box.minX, box.minY, box.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(box.minX, box.minY, box.minZ).color(r, g, b, a).endVertex();
        buffer.pos(box.minX, box.maxY, box.minZ).color(r, g, b, a).endVertex();
        buffer.pos(box.maxX, box.maxY, box.minZ).color(r, g, b, a).endVertex();
        buffer.pos(box.maxX, box.maxY, box.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(box.minX, box.maxY, box.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(box.minX, box.maxY, box.minZ).color(r, g, b, a).endVertex();
        buffer.pos(box.minX, box.maxY, box.maxZ).color(r, g, b, 0.0f).endVertex();
        buffer.pos(box.minX, box.minY, box.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(box.maxX, box.maxY, box.maxZ).color(r, g, b, 0.0f).endVertex();
        buffer.pos(box.maxX, box.minY, box.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(box.maxX, box.maxY, box.minZ).color(r, g, b, 0.0f).endVertex();
        buffer.pos(box.maxX, box.minY, box.minZ).color(r, g, b, a).endVertex();
        buffer.pos(box.maxX, box.minY, box.minZ).color(r, g, b, 0.0f).endVertex();
        tessellator.draw();
    }

    /**
     * Draws an entity's bounding box when given an entity
     *
     * @param entity       The entity to use the bounding box of
     * @param c            the color
     * @param partialTicks the partialTicks of the Render Event
     */
    public static void drawEntityBoundingBox(Entity entity, int c, Float partialTicks) {
        float r = (float) (c >> 16 & 255) / 255.0F;
        float g = (float) (c >> 8 & 255) / 255.0F;
        float b = (float) (c & 255) / 255.0F;
        float a = (float) (c >> 24 & 255) / 255.0F;
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.glLineWidth(1.0f);
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        if (entity != mc.player) {
            double x = mc.player.lastTickPosX + (mc.player.posX - mc.player.lastTickPosX) * partialTicks;
            double y = mc.player.lastTickPosY + (mc.player.posY - mc.player.lastTickPosY) * partialTicks;
            double z = mc.player.lastTickPosZ + (mc.player.posZ - mc.player.lastTickPosZ) * partialTicks;
            drawBoundingBox(entity.getEntityBoundingBox().offset(-x, -y, -z), r, g, b, a);
        }
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void drawTileEntityBoundingBox(TileEntity entity, int c) {
        float r = (float) (c >> 16 & 255) / 255.0F;
        float g = (float) (c >> 8 & 255) / 255.0F;
        float b = (float) (c & 255) / 255.0F;
        float a = (float) (c >> 24 & 255) / 255.0F;

        GlStateManager.pushMatrix();
        prepareBoxRender();
        GlStateManager.enableBlend();
        GlStateManager.glLineWidth(3.0f);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();

        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        final BlockPos entityBlockPos = entity.getPos();

        final IBlockState iBlockState = mc.world.getBlockState(entityBlockPos);

        AxisAlignedBB boundingBox = iBlockState.getSelectedBoundingBox(mc.world, entityBlockPos).offset(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ);

        drawBoundingBox(boundingBox.grow(0.0020000000949949026D), r, g, b, a);

        GlStateManager.resetColor();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        releaseBoxRender();
        GlStateManager.popMatrix();
    }

    public static void renderRectangle(float left, float top, float right, float bottom, int colour) {
        drawRect0(left, top, right, bottom, colour, GL_QUADS);
    }

    public static void drawRect0(float left, float top, float right, float bottom, int colour, int glMode) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        colour(colour);
        bufferbuilder.begin(glMode, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(left, bottom, 0.0).endVertex();
        bufferbuilder.pos(right, bottom, 0.0).endVertex();
        bufferbuilder.pos(right, top, 0.0).endVertex();
        bufferbuilder.pos(left, top, 0.0).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void colour(int rgb) {
        float r = (rgb >> 16 & 255) / 255.0f;
        float g = (rgb >> 8 & 255) / 255.0f;
        float b = (rgb & 255) / 255.0f;
        float a = (rgb >> 24 & 255) / 255.0f;
        GlStateManager.color(r, g, b, a);
    }

    public static void renderOutline(float left, float top, float right, float bottom, int colour) {
        drawRect0(left, top, right, bottom, colour, GL_LINE_LOOP);
    }

    public static void drawOutlineBox(BlockPos pos, Float red, Float green, Float blue, Float alpha, Float partialTicks) {
        drawOutlineBox(new Vec3d(pos.getX(), pos.getY(), pos.getZ()), red, green, blue, alpha, 1, 1, 1, partialTicks);
    }

    public static void drawOutlineBox(Vec3d inputPos, Float red, Float green, Float blue, Float alpha, double width, double height, double depth, Float partialTicks) {
        double x = mc.player.lastTickPosX + (mc.player.posX - mc.player.lastTickPosX) * partialTicks;
        double y = mc.player.lastTickPosY + (mc.player.posY - mc.player.lastTickPosY) * partialTicks;
        double z = mc.player.lastTickPosZ + (mc.player.posZ - mc.player.lastTickPosZ) * partialTicks;
        Vec3d pos = inputPos.add(-x, -y, -z);
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
        //drawBoundingBox(bufferbuilder, minX, minY, minZ, maxX, maxY, maxZ, red, green, blue, alpha);
        buffer.pos(pos.x, pos.y, pos.z).color(red, green, blue, 0.0f).endVertex();
        buffer.pos(pos.x, pos.y, pos.z).color(red, green, blue, alpha).endVertex();
        buffer.pos(pos.x + width, pos.y, pos.z).color(red, green, blue, alpha).endVertex();
        buffer.pos(pos.x + width, pos.y, pos.z + depth).color(red, green, blue, alpha).endVertex();
        buffer.pos(pos.x, pos.y, pos.z + depth).color(red, green, blue, alpha).endVertex();
        buffer.pos(pos.x, pos.y, pos.z).color(red, green, blue, alpha).endVertex();
        buffer.pos(pos.x, pos.y + height, pos.z).color(red, green, blue, alpha).endVertex();
        buffer.pos(pos.x + width, pos.y + height, pos.z).color(red, green, blue, alpha).endVertex();
        buffer.pos(pos.x + width, pos.y + height, pos.z + depth).color(red, green, blue, alpha).endVertex();
        buffer.pos(pos.x, pos.y + height, pos.z + depth).color(red, green, blue, alpha).endVertex();
        buffer.pos(pos.x, pos.y + height, pos.z).color(red, green, blue, alpha).endVertex();
        buffer.pos(pos.x, pos.y + height, pos.z + depth).color(red, green, blue, 0.0f).endVertex();
        buffer.pos(pos.x, pos.y, pos.z + depth).color(red, green, blue, alpha).endVertex();
        buffer.pos(pos.x + width, pos.y + height, pos.z + depth).color(red, green, blue, 0.0f).endVertex();
        buffer.pos(pos.x + width, pos.y, pos.z + depth).color(red, green, blue, alpha).endVertex();
        buffer.pos(pos.x + width, pos.y + height, pos.z).color(red, green, blue, 0.0f).endVertex();
        buffer.pos(pos.x + width, pos.y, pos.z).color(red, green, blue, alpha).endVertex();
        buffer.pos(pos.x + width, pos.y, pos.z).color(red, green, blue, 0.0f).endVertex();
        tessellator.draw();
    }

    public static void drawBox(BlockPos blockPos, int red, int green, int blue, int alpha, double width, double height, double distance, float partialTicks) {
        double x = blockPos.getX() - (mc.player.lastTickPosX + (mc.player.posX - mc.player.lastTickPosX) * (double) partialTicks);
        double y = blockPos.getY() - (mc.player.lastTickPosY + (mc.player.posY - mc.player.lastTickPosY) * (double) partialTicks);
        double z = blockPos.getZ() - (mc.player.lastTickPosZ + (mc.player.posZ - mc.player.lastTickPosZ) * (double) partialTicks);

/*      GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.glLineWidth(10.0f);
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);*/
        GlStateManager.color(red / 255f, green / 255f, blue / 255f, alpha / 255f);
        AxisAlignedBB axisalignedbb = new AxisAlignedBB(x, y, z, x + width, y + height, z + distance);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);// Starts X.
        buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();// Ends X.
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);// Starts Y.
        buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();// Ends Y.
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);// Starts Z.
        buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();// Ends Z.

/*        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();*/
    }

    public static void drawBox(Vec3d vec, int red, int green, int blue, int alpha, double width, double height, double distance, float partialTicks) {
        double x = vec.x - (mc.player.lastTickPosX + (mc.player.posX - mc.player.lastTickPosX) * (double) partialTicks);
        double y = vec.y - (mc.player.lastTickPosY + (mc.player.posY - mc.player.lastTickPosY) * (double) partialTicks);
        double z = vec.z - (mc.player.lastTickPosZ + (mc.player.posZ - mc.player.lastTickPosZ) * (double) partialTicks);

/*      GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.glLineWidth(10.0f);
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);*/
        GlStateManager.color(red / 255f, green / 255f, blue / 255f, alpha / 255f);
        AxisAlignedBB axisalignedbb = new AxisAlignedBB(x, y, z, x + width, y + height, z + distance);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);// Starts X.
        buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();// Ends X.
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);// Starts Y.
        buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();// Ends Y.
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);// Starts Z.
        buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();// Ends Z.

/*        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();*/
    }

    public static void drawLine(float x, float y, float z, float x1, float y1, float z1, float thickness, int hex) {
        final Float[] rgb = RenderUtils.hexToRGB(hex);
        glLineWidth(thickness);
        glEnable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x, y, z).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        buffer.pos(x1, y1, z1).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        buffer.pos(x1, y1, z1).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        tessellator.draw();
        glDisable(GL_LINE_SMOOTH);
    }

    public static Vec3d interpolateEntity(Entity entity, float partialTicks) {
        return new Vec3d(entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks,
                entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks,
                entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks);
    }

    public static void prepareBoxRender() {
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.glLineWidth(10.0f);
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.resetColor();
    }

    public static void releaseBoxRender() {
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.resetColor();
    }

    public static void prepareRender() {
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.resetColor();
    }

    public static void releaseRender() {
        GlStateManager.resetColor();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
    }

    public static Float[] hexToRGB(int hex) {
        float red = (hex >> 16 & 0xFF) / 255.0F;
        float green = (hex >> 8 & 0xFF) / 255.0F;
        float blue = (hex & 0xFF) / 255.0F;
        float alpha = (hex >> 24 & 0xFF) / 255.0F;
        return new Float[]{red, green, blue, alpha};
    }

    //I should have pasted this forever ago, I was doing it manually
    public static Vec3d getInterpolatedPos(Entity entity, float ticks) {
        return new Vec3d(entity.lastTickPosX, entity.lastTickPosY, entity.lastTickPosZ).add(getInterpolatedAmount(entity, ticks));
    }

    public static Vec3d getInterpolatedRenderPos(Entity entity, float ticks) {
        return getInterpolatedPos(entity, ticks).subtract(((IRenderManager) mc.getRenderManager()).getRenderPosX(), ((IRenderManager) mc.getRenderManager()).getRenderPosY(), ((IRenderManager) mc.getRenderManager()).getRenderPosZ());
    }

    public static Vec3d getInterpolatedAmount(Entity entity, double x, double y, double z) {
        return new Vec3d(
                (entity.posX - entity.lastTickPosX) * x,
                (entity.posY - entity.lastTickPosY) * y,
                (entity.posZ - entity.lastTickPosZ) * z
        );
    }

    public static Vec3d getInterpolatedAmount(Entity entity, double ticks) {
        return getInterpolatedAmount(entity, ticks, ticks, ticks);
    }

    public static Vec3d getInterpolatedRenderPos(Vec3d vec) {
        return new Vec3d(vec.x, vec.y, vec.z).subtract(((IRenderManager) mc.getRenderManager()).getRenderPosX(), ((IRenderManager) mc.getRenderManager()).getRenderPosY(), ((IRenderManager) mc.getRenderManager()).getRenderPosZ());
    }

    public static void glBillboard(float x, float y, float z) {
        float scale = 0.016666668f * 1.6f;
        GlStateManager.translate(x - ((IRenderManager) mc.getRenderManager()).getRenderPosX(), y - ((IRenderManager) mc.getRenderManager()).getRenderPosY(), z - ((IRenderManager) mc.getRenderManager()).getRenderPosZ());
        GlStateManager.glNormal3f(0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(-Minecraft.getMinecraft().player.rotationYaw, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(Minecraft.getMinecraft().player.rotationPitch, Minecraft.getMinecraft().gameSettings.thirdPersonView == 2 ? -1.0f : 1.0f, 0.0f, 0.0f);
        GlStateManager.scale(-scale, -scale, scale);
    }

    public static void glBillboardDistanceScaled(float x, float y, float z, EntityPlayer player, float scale) {
        glBillboard(x, y, z);
        int distance = (int) player.getDistance(x, y, z);
        float scaleDistance = (distance / 2.0f) / (2.0f + (2.0f - scale));
        if (scaleDistance < 1f)
            scaleDistance = 1;
        GlStateManager.scale(scaleDistance, scaleDistance, scaleDistance);
    }
}
