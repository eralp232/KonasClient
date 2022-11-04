package me.darki.konas.module.modules.render;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.Render2DEvent;
import me.darki.konas.event.events.RenderAttackIndicatorEvent;
import me.darki.konas.event.events.UpdateWalkingPlayerEvent;
import me.darki.konas.mixin.mixins.IMinecraft;
import me.darki.konas.mixin.mixins.ITimer;
import me.darki.konas.module.Module;
import me.darki.konas.setting.ColorSetting;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.client.InterpolationHelper;
import me.darki.konas.util.math.Interpolation;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class Crosshair extends Module {
    private final Setting<Boolean> dot = new Setting<>("Dot", false);

    private final Setting<Float> crosshairGap = new Setting<>("Gap", 2F, 10F, 0F, 0.5F);
    private final Setting<Float> motionGap = new Setting<>("MotionGap", 0F, 5F, 0F, 0.05F);
    private final Setting<Float> crosshairWidth = new Setting<>("Width", 1F, 5F, 0.1F, 0.1F);
    private final Setting<Float> motionWidth = new Setting<>("MotionWidth", 0F, 2.5F, 0F, 0.05F);
    private final Setting<Float> crosshairSize = new Setting<>("Size", 2F, 40F, 1F, 0.5F);
    private final Setting<Float> motionSize = new Setting<>("MotionSize", 0F, 20F, 0F, 0.2F);

    private final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(0xFFFFFFFF));

    public Crosshair() {
        super("Crosshair", "Draws a custom crosshair", Category.RENDER);
    }

    private long lastUpdate = -1L;
    private float currentMotion = 0F;
    private float prevMotion = 0F;

    @Subscriber
    public void onRenderAttackIndicator(RenderAttackIndicatorEvent event) {
        event.setCancelled(true);
    }

    @Subscriber
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent.Pre event) {
        prevMotion = currentMotion;
        double dX = mc.player.posX - mc.player.prevPosX;
        double dZ = mc.player.posZ - mc.player.prevPosZ;
        currentMotion = (float) Math.sqrt(dX * dX + dZ * dZ);
        lastUpdate = System.currentTimeMillis();
    }

    @Subscriber
    public void onRender2D(Render2DEvent event) {
        ScaledResolution sr = new ScaledResolution(mc);
        float cX = (float) (sr.getScaledWidth_double() / 2F + 0.5F);
        float cY = (float) (sr.getScaledHeight_double() / 2F + 0.5F);
        float gap = crosshairGap.getValue();
        float width = Math.max(crosshairWidth.getValue(), 0.5F);
        float size = crosshairSize.getValue();
        float tickLength = ((ITimer) ((IMinecraft) mc).getTimer()).getTickLength();
        gap += Interpolation.lerp(prevMotion, currentMotion, Math.min((System.currentTimeMillis() - lastUpdate) / tickLength, 1F)) * motionGap.getValue();
        width += Interpolation.lerp(prevMotion, currentMotion, Math.min((System.currentTimeMillis() - lastUpdate) / tickLength, 1F)) * motionWidth.getValue();
        size += Interpolation.lerp(prevMotion, currentMotion, Math.min((System.currentTimeMillis() - lastUpdate) / tickLength, 1F)) * motionSize.getValue();
        drawRect(cX - gap - size,cY - width / 2.0F,cX - gap,cY + width / 2.0F, color.getValue().getColor());
        drawRect(cX + gap + size,cY - width / 2.0F,cX + gap,cY + width / 2.0F, color.getValue().getColor());
        drawRect(cX - width / 2.0F,cY + gap + size,cX + width / 2.0F,cY + gap, color.getValue().getColor());
        drawRect(cX - width / 2.0F,cY - gap - size,cX + width / 2.0F,cY - gap, color.getValue().getColor());
        if (dot.getValue()) {
            drawRect(cX - width / 2F, cY - width / 2F, cX + width / 2F, cY + width / 2F, color.getValue().getColor());
        }
    }

    public static void drawRect(float left, float top, float right, float bottom, int color)
    {
        if (left < right)
        {
            float i = left;
            left = right;
            right = i;
        }

        if (top < bottom)
        {
            float j = top;
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
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos((double)left, (double)bottom, 0.0D).endVertex();
        bufferbuilder.pos((double)right, (double)bottom, 0.0D).endVertex();
        bufferbuilder.pos((double)right, (double)top, 0.0D).endVertex();
        bufferbuilder.pos((double)left, (double)top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
}
