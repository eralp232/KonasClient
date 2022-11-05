package me.darki.konas.module.modules.render;

import com.google.common.primitives.Ints;
import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.Render2DEvent;
import me.darki.konas.event.events.Render3DEvent;
import me.darki.konas.mixin.mixins.IEntityRenderer;
import me.darki.konas.mixin.mixins.IRenderManager;
import me.darki.konas.module.Module;
import me.darki.konas.setting.ColorSetting;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.KonasGlobals;
import me.darki.konas.util.friends.Friends;
import me.darki.konas.util.render.RenderUtil;
import me.darki.konas.util.render.RenderUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

public class Tracers extends Module {

    private static final Setting<Mode> mode = new Setting<>("Mode", Mode.LINES);
    private static final Setting<Boolean> showTargets = new Setting<>("ShowTargets", true);
    private static final Setting<Boolean> showDistanceColor = new Setting<>("ShowDistanceColor", true);
    private static final Setting<Boolean> showFriends = new Setting<>("ShowFriends", true);
    private static final Setting<ColorSetting> colorSetting = new Setting<>("Color", new ColorSetting(0xFFFFFFFF));
    private static final Setting<Boolean> drawVisible = new Setting<>("Visible", false).withVisibility(() -> mode.getValue() == Mode.ARROWS);
    private static final Setting<Boolean> fade = new Setting<>("Fade", false).withVisibility(() -> mode.getValue() == Mode.ARROWS);
    private static final Setting<Integer> fadeDistance = new Setting<>("Distance", 100, 200, 50, 1).withVisibility(() -> mode.getValue() == Mode.ARROWS && fade.getValue());
    private static final Setting<Integer> radius = new Setting<>("Radius", 30, 200, 10, 1);
    private static final Setting<Float> width = new Setting<>("Width", 2f, 5f, 0.1f, 0.5f);
    private static final Setting<Float> tracerRange = new Setting<>("Range", 220f, 500f, 1f, 1f);

    private enum Mode {
        LINES, ARROWS
    }

    public Tracers() {
        super("Tracers", Category.RENDER);
    }

    @Subscriber
    public void onRender2D(Render2DEvent event) {
        if (mc.world == null || mc.player == null) return;

        if (mode.getValue() == Mode.LINES) return;

        for (Entity e : mc.world.loadedEntityList) {

            if (e instanceof EntityPlayer && e != mc.player) {
                if (mc.player.getDistance(e) <= tracerRange.getValue()) {
                    Vec3d ePos = new Vec3d(e.lastTickPosX + (e.posX - e.lastTickPosX) * mc.getRenderPartialTicks(),
                            e.lastTickPosY + (e.posY - e.lastTickPosY) * mc.getRenderPartialTicks(),
                            e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * mc.getRenderPartialTicks()).add(0, e.getEyeHeight(), 0);

                    Vec3d pos = KonasGlobals.INSTANCE.vectorUtils.toScaledScreenPos(ePos);

                    if (pos != null && !isOnScreen(pos) && (!RenderUtil.isInViewFrustrum(e) || drawVisible.getValue())) {
                        GL11.glPushMatrix();
                        int rawColor = 0xFFFFFFFF;
                        if (showTargets.getValue() && KonasGlobals.INSTANCE.targetManager.isTarget(e)) {
                            int lifespan = KonasGlobals.INSTANCE.targetManager.getTargetLifespanColor(e);
                            rawColor = new Color(255, lifespan, lifespan).hashCode();
                        } else {
                            rawColor = (Friends.isFriend(e.getName()) && showFriends.getValue()) ?
                                    Color.CYAN.hashCode() : (showDistanceColor.getValue() ?
                                    distanceColor(e.getDistance(mc.player)) : colorSetting.getValue().getColor());
                        }
                        int alpha = (rawColor >> 24) & 0xff;
                        int red = (rawColor >> 16) & 0xFF;
                        int green = (rawColor >> 8) & 0xFF;
                        int blue = (rawColor) & 0xFF;
                        Color color = new Color((int) red, (int) green, (int) blue, (int) (fade.getValue() ? MathHelper.clamp(255.0F - 255.0F / fadeDistance.getValue() * mc.player.getDistance(e), 100.0F, 255.0F) : alpha));
                        int x = Display.getWidth() / 2 / ((mc.gameSettings.guiScale == 0) ? 1 : mc.gameSettings.guiScale);
                        int y = Display.getHeight() / 2 / ((mc.gameSettings.guiScale == 0) ? 1 : mc.gameSettings.guiScale);
                        float yaw = getRotations(e) - mc.player.rotationYaw;
                        GL11.glTranslatef(x, y, 0.0F);
                        GL11.glRotatef(yaw, 0.0F, 0.0F, 1.0F);
                        GL11.glTranslatef(-x, -y, 0.0F);
                        RenderUtil.drawTracerPointer(x, y - radius.getValue(), width.getValue() * 5F, 2.0F, 1.0F, false, 1F, color.getRGB());
                        GL11.glTranslatef(x, y, 0.0F);
                        GL11.glRotatef(-yaw, 0.0F, 0.0F, 1.0F);
                        GL11.glTranslatef(-x, -y, 0.0F);
                        GL11.glColor4f(1F, 1F, 1F, 1F);
                        GL11.glPopMatrix();
                    }
                }
            }
        }
        GL11.glColor4f(1F, 1F, 1F, 1F);
    }

    @Subscriber
    public void onRender3D(Render3DEvent event) {

        if (mc.world == null || mc.player == null) return;

        if (mode.getValue() == Mode.ARROWS) return;

        for (Entity e : mc.world.loadedEntityList) {

            if (e instanceof EntityPlayer && e != mc.player) {

                if (mc.player.getDistance(e) <= tracerRange.getValue()) {
                    final Vec3d pos = RenderUtils.interpolateEntity(e, event.getPartialTicks()).subtract(((IRenderManager) mc.getRenderManager()).getRenderPosX(), ((IRenderManager) mc.getRenderManager()).getRenderPosY(), ((IRenderManager) mc.getRenderManager()).getRenderPosZ());

                    //don't know if some of this stuff is needed, I just am too tired to mess with it
                    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                    GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                    GlStateManager.glLineWidth(width.getValue());
                    GlStateManager.disableTexture2D();
                    GlStateManager.depthMask(false);
                    GlStateManager.enableBlend();
                    GlStateManager.disableDepth();
                    GlStateManager.disableLighting();
                    GlStateManager.disableCull();
                    GlStateManager.enableAlpha();
                    GlStateManager.color(1, 1, 1);
                    final boolean bobbing = mc.gameSettings.viewBobbing;
                    mc.gameSettings.viewBobbing = false;
                    ((IEntityRenderer) mc.entityRenderer).iSetupCameraTransform(event.getPartialTicks(), 0);
                    final Vec3d forward = new Vec3d(0, 0, 1).rotatePitch(-(float) Math.toRadians(mc.player.rotationPitch)).rotateYaw(-(float) Math.toRadians(mc.player.rotationYaw));
                    int color;
                    if (showTargets.getValue() && KonasGlobals.INSTANCE.targetManager.isTarget(e)) {
                        int lifespan = KonasGlobals.INSTANCE.targetManager.getTargetLifespanColor(e);
                        color = new Color(255, lifespan, lifespan).hashCode();
                    } else {
                        color = (Friends.isFriend(e.getName()) && showFriends.getValue()) ?
                                Color.CYAN.hashCode() : (showDistanceColor.getValue() ?
                                distanceColor(e.getDistance(mc.player)) : colorSetting.getValue().getColor());
                    }
                    RenderUtils.drawLine((float) forward.x, (float) forward.y + mc.player.getEyeHeight(), (float) forward.z, (float) pos.x, (float) pos.y, (float) pos.z, width.getValue(), color);
                    RenderUtils.drawLine((float) pos.x, (float) pos.y, (float) pos.z, (float) pos.x, (float) pos.y + e.getEyeHeight(), (float) pos.z, width.getValue(), color);
                    mc.gameSettings.viewBobbing = bobbing;
                    ((IEntityRenderer) mc.entityRenderer).iSetupCameraTransform(event.getPartialTicks(), 0);
                    GlStateManager.enableCull();
                    GlStateManager.depthMask(true);
                    GlStateManager.enableTexture2D();
                    GlStateManager.enableBlend();
                    GlStateManager.enableDepth();
                }

            }
        }
    }

    private float getRotations(Entity entity) {
        double x = entity.posX - mc.player.posX;
        double z = entity.posZ - mc.player.posZ;
        return (float)-(Math.atan2(x, z) * (180 / Math.PI));
    }

    private boolean isOnScreen(Vec3d pos) {
        if (pos.x > -1.0D && pos.y < 1.0D)
            return (pos.x / ((mc.gameSettings.guiScale == 0) ? 1 : mc.gameSettings.guiScale) >= 0.0D && pos.x / ((mc.gameSettings.guiScale == 0) ? 1 : mc.gameSettings.guiScale) <= Display.getWidth() && pos.y / ((mc.gameSettings.guiScale == 0) ? 1 : mc.gameSettings.guiScale) >= 0.0D && pos.y / ((mc.gameSettings.guiScale == 0) ? 1 : mc.gameSettings.guiScale) <= Display.getHeight());
        return false;
    }


    @SuppressWarnings("UnstableApiUsage")
    private int distanceColor(float distance) {
        int c = Ints.constrainToRange((int) distance * 4, 0, 255);
        return new Color(
                Ints.constrainToRange(255 - c, 0, 255),
                Ints.constrainToRange(c, 0, 255),
                0
        ).hashCode();
    }

}
