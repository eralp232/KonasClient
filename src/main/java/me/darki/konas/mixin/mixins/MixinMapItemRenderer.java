package me.darki.konas.mixin.mixins;

import cookiedragon.eventsystem.EventDispatcher;
import me.darki.konas.event.events.RenderMapEvent;
import net.minecraft.client.gui.MapItemRenderer;
import net.minecraft.world.storage.MapData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MapItemRenderer.class)
public class MixinMapItemRenderer {
    @Inject(method = "renderMap", at = @At("HEAD"), cancellable = true)
    public void onRenderMap(MapData mapdataIn, boolean noOverlayRendering, CallbackInfo ci) {
        RenderMapEvent event = new RenderMapEvent();
        EventDispatcher.Companion.dispatch(event);
        if (event.isCancelled()) ci.cancel();
    }
}
