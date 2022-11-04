package me.darki.konas.module.modules.render;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.Render3DEvent;
import me.darki.konas.event.events.UpdateEvent;
import me.darki.konas.event.events.WorldClientInitEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.ColorSetting;
import me.darki.konas.setting.Setting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;

public class Breadcrumbs extends Module {

    public static ArrayList<Vec3d> vertices = new ArrayList<>();

    public static Setting<Boolean> onlyRender = new Setting<>("OnlyRender", false);
    private Setting<Integer> maxVertices = new Setting<>("MaxVertices", 50, 250, 25, 25);
    private Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(Color.WHITE.hashCode()));

    public Breadcrumbs() {
        super("Breadcrumbs", Category.RENDER);
    }

    @Subscriber
    public void onUpdate(UpdateEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (onlyRender.getValue()) return;
        if (mc.player.posX != mc.player.lastTickPosX || mc.player.posY != mc.player.lastTickPosY || mc.player.posZ != mc.player.lastTickPosZ) {
            vertices.add(mc.player.getPositionVector());
            if (vertices.size() >= maxVertices.getValue() * 10000) {
                vertices.remove(0);
                vertices.remove(1);
            }
        }
    }

    @Subscriber
    public void onWorldJoin(WorldClientInitEvent event) {
        vertices.clear();
    }

    @Subscriber
    public void onRender(Render3DEvent event) {
        if (mc.player == null || mc.world == null) return;

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.glLineWidth(1.5F);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableAlpha();

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GL11.glEnable(2848);
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GL11.glHint(3154, 4354);
        GlStateManager.depthMask(false);
        GlStateManager.color(color.getValue().getColorObject().getRed() / 255.0F, color.getValue().getColorObject().getGreen() / 255.0F, color.getValue().getColorObject().getBlue() / 255.0F, color.getValue().getColorObject().getAlpha() / 255.0F);

        double x = (mc.player.lastTickPosX + (mc.player.posX - mc.player.lastTickPosX) * event.getPartialTicks());
        double y = (mc.player.lastTickPosY + (mc.player.posY - mc.player.lastTickPosY) * event.getPartialTicks());
        double z = (mc.player.lastTickPosZ + (mc.player.posZ - mc.player.lastTickPosZ) * event.getPartialTicks());

        //GL11.glTranslated(x - ((IRenderManager) mc.getRenderManager()).getRenderPosX(), y - ((IRenderManager) mc.getRenderManager()).getRenderPosY(), z - ((IRenderManager) mc.getRenderManager()).getRenderPosZ());

        GL11.glBegin(GL_LINE_STRIP);
        for (Vec3d vertex : vertices) {
            Vec3d vec = vertex.subtract(x, y, z);
            GL11.glVertex3d(vec.x, vec.y, vec.z);
        }
        GL11.glEnd();

        GL11.glPopMatrix();

        GlStateManager.enableCull();
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.enableDepth();
    }
}
