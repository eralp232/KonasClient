package me.darki.konas.gui.kgui.render;

import me.darki.konas.command.commands.FontCommand;
import me.darki.konas.gui.kgui.fill.Fill;
import me.darki.konas.gui.kgui.shape.AbstractShape;
import me.darki.konas.util.render.font.CustomFontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class Renderer {
    public static CustomFontRenderer subFontRendrer = new CustomFontRenderer(FontCommand.lastFont, 16);
    public static CustomFontRenderer fontRenderer = new CustomFontRenderer(FontCommand.lastFont, 18);

    public static void drawShape(AbstractShape abstractShape, Fill fill, float width) {
        Vector2f[] points = abstractShape.getPoints();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        GlStateManager.glLineWidth(width);

        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        bufferBuilder.begin(abstractShape.isClosed() ? GL11.GL_LINE_LOOP : GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for (Vector2f point : points) {
            Color color = fill.colorAt(abstractShape, point.x, point.y);
            bufferBuilder.pos(point.x, point.y, 0.0D).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        }

        tessellator.draw();

        GL11.glDisable(GL11.GL_LINE_SMOOTH);

        GlStateManager.glLineWidth(1F);

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void fillShape(AbstractShape abstractShape, Fill fill) {
        Vector2f[] points = abstractShape.getPoints();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        bufferBuilder.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_COLOR);
        for (Vector2f point : points) {
            Color color = fill.colorAt(abstractShape, point.x, point.y);
            bufferBuilder.pos(point.x, point.y, 0.0D).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        }

        tessellator.draw();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
}
