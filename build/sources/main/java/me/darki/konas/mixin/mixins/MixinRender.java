package me.darki.konas.mixin.mixins;

import cookiedragon.eventsystem.EventDispatcher;
import me.darki.konas.event.events.RenderPlayerShadowEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Render.class)
public class MixinRender {
    @Inject(method = "renderShadow", at = @At("HEAD"), cancellable = true)
    public void injectRenderShadow(Entity entityIn, double x, double y, double z, float shadowAlpha, float partialTicks, CallbackInfo ci) {
        if (Minecraft.getMinecraft().player != null && entityIn.equals(Minecraft.getMinecraft().player)) {
            RenderPlayerShadowEvent event = new RenderPlayerShadowEvent();
            EventDispatcher.Companion.dispatch(event);
            if (event.isCancelled()) {
                ci.cancel();
            }
        }
    }
}
