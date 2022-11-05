package me.darki.konas.module.modules.player;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.Render3DEvent;
import me.darki.konas.mixin.mixins.IRenderManager;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.math.Interpolation;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

public class YawLock extends Module {
    private static Setting<Boolean> diagonal = new Setting<>("Diagonals", true);
    private static Setting<Boolean> render = new Setting<>("Render", true);
    private static Setting<Boolean> entities = new Setting<>("Entities", false);
    private static Setting<Float> interpSpeed = new Setting<>("Speed", 0.1f, 5f, 0f, 0.1f);

    private int ignoreTicks = 0;
    private float heightProgress = 0f;

    public YawLock() {
        super("YawLock", "Lock your yaw rotation", Category.PLAYER);
    }

    @Subscriber
    public void onRender3D(Render3DEvent event) {
        if (mc.player == null) return;;

        if (mc.mouseHelper.deltaX != 0 || mc.mouseHelper.deltaY != 0 || isAnyMouseButtonDown()) {
            ignoreTicks = 4;
        } else {
            float diff = 360 / (diagonal.getValue() ? 8f : 4f);
            if (ignoreTicks <= 0) {
                float yaw = mc.player.rotationYaw + 180F;
                yaw = Math.round((yaw / diff)) * diff;
                yaw -= 180F;
                mc.player.prevRotationYaw = mc.player.rotationYaw;
                mc.player.rotationYaw = Interpolation.finterpTo(
                        mc.player.rotationYaw,
                        yaw,
                        mc.getRenderPartialTicks(),
                        interpSpeed.getValue()
                );
                if (entities.getValue()) {
                    if (mc.player.isRiding()) {
                        mc.player.getRidingEntity().prevRotationYaw = mc.player.getRidingEntity().rotationYaw;
                        mc.player.getRidingEntity().rotationYaw = mc.player.rotationYaw;
                    }
                }
            } else {
                ignoreTicks -= 1;
            }
        }

        if (render.getValue() && (ignoreTicks > 0 || heightProgress > 0)) {
            double distance = 300.0;

            Vec3d root = mc.player.getPositionVector();
            Vec3d[] positions;
            if (diagonal.getValue()) {
                 positions = new Vec3d[]{
                        root.add(distance, 0.0, 0.0),
                        root.add(distance / 2, 0.0, distance / 2),
                        root.add(0.0, 0.0, distance),
                        root.add(-distance / 2, 0.0, distance / 2),
                        root.add(-distance, 0.0, 0.0),
                        root.add(-distance / 2, 0.0, -distance / 2),
                        root.add(0.0, 0.0, -distance),
                        root.add(distance / 2, 0.0, -distance / 2)
                };
            } else {
                positions = new Vec3d[]{
                        root.add(distance, 0.0, 0.0),
                        root.add(0.0, 0.0, distance),
                        root.add(-distance, 0.0, 0.0),
                        root.add(0.0, 0.0, -distance)
                };
            }

            if (ignoreTicks > 0) {
                heightProgress = Interpolation.finterpTo(heightProgress, 255f, mc.getRenderPartialTicks(), interpSpeed.getValue());
            } else if (heightProgress > 0) {
                heightProgress = Interpolation.finterpTo(heightProgress, 0f, mc.getRenderPartialTicks(), interpSpeed.getValue());
            }

            if (heightProgress != 0f) {
                GlStateManager.pushMatrix();
                GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                GlStateManager.enableBlend();
                GlStateManager.disableLighting();
                GlStateManager.disableCull();
                GlStateManager.color(1F, 1F, 1F, 1F);
                GlStateManager.disableDepth();
                GlStateManager.depthMask(false);
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
                GlStateManager.glLineWidth(2f);
                GlStateManager.disableTexture2D();
                GL11.glEnable(GL11.GL_LINE_SMOOTH);
                GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);

                double renderX = ((IRenderManager) mc.getRenderManager()).getRenderPosX();
                double renderY = ((IRenderManager) mc.getRenderManager()).getRenderPosY();
                double renderZ = ((IRenderManager) mc.getRenderManager()).getRenderPosZ();

                GlStateManager.translate(-renderX, -renderY, -renderZ);

                for (Vec3d position : positions) {
                   drawLine(
                            position.subtract(0.0, heightProgress, 0.0),
                            position.add(0.0, heightProgress, 0.0),
                            0.96f,
                            0.19f,
                            0.19f,
                            ((heightProgress / 255f) / 2) + 127.5f
                    );
                }
                // Disable gl flags
                GlStateManager.shadeModel(GL11.GL_FLAT);
                GL11.glDisable(GL11.GL_LINE_SMOOTH);
                GlStateManager.enableDepth();
                GlStateManager.depthMask(true);
                GlStateManager.color(1F, 1F, 1F, 1F);
                GlStateManager.enableCull();
                GlStateManager.enableLighting();
                GlStateManager.disableBlend();
                GlStateManager.enableTexture2D();
                GlStateManager.popMatrix();
            }
        }
    }

    public static void drawLine(Vec3d startPos, Vec3d endPos, int color, boolean smooth, float width) {
        drawLine(startPos, endPos, color);
    }

    public static void drawLine(Vec3d startPos, Vec3d endPos, int color) {
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        float alpha = (float) (color >> 24 & 255) / 255.0F;
        drawLine(startPos, endPos, red, green, blue, alpha);
    }

    public static void drawLine(Vec3d startPos, Vec3d endPos, float red, float green, float blue, float alpha) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(startPos.x, startPos.y, startPos.z).color(red, green, blue, alpha).endVertex();
        buffer.pos(endPos.x, endPos.y, endPos.z).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
    }

    private boolean isAnyMouseButtonDown() {
        for (int i = 0; i < Mouse.getButtonCount(); i++) {
            if (Mouse.isButtonDown(i)) {
                return true;
            }
        }
        return false;
    }
}
