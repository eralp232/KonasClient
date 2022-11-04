package me.darki.konas.module.modules.render;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.UpdateEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import java.util.Locale;

public class Shaders extends Module {
    private Setting<Mode> shader = new Setting<>("Shader", Mode.ANTIALIAS);

    private enum Mode {
        ANTIALIAS, ART, BITS, BLOBS, BLOBS2, BLUR, BUMPY, COLOR_CONVOLVE, CREEPER, DECONVERGE, DESATURATE, ENTITY_OUTLINE, FLIP, FXAA, GREEN, INVERT, NOTCH, NTSC, OUTLINE, PENCIL, PHOSPHOR, SCAN_PINCUSION, SOBEL, SPIDER, WOBBLE
    }
    
    public Shaders() {
        super("Shaders", "Enable 1.8 shaders", Category.RENDER);
    }

    public void onDisable() {
        if (mc.entityRenderer.getShaderGroup() != null) {
            mc.entityRenderer.getShaderGroup().deleteShaderGroup();
        }
    }
    
    @Subscriber
    public void onUpdate(UpdateEvent event) {
        if (OpenGlHelper.shadersSupported && mc.getRenderViewEntity() instanceof EntityPlayer) {
            if (mc.entityRenderer.getShaderGroup() != null) {
                mc.entityRenderer.getShaderGroup().deleteShaderGroup();
            }
            try {
                mc.entityRenderer.loadShader(new ResourceLocation("shaders/post/" + this.shader.getValue().name().toLowerCase(Locale.ROOT) + ".json"));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if (mc.entityRenderer.getShaderGroup() != null && mc.currentScreen == null) {
            mc.entityRenderer.getShaderGroup().deleteShaderGroup();
        }
    }

}
