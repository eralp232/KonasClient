package me.darki.konas.mixin.mixins;

import cookiedragon.eventsystem.EventDispatcher;
import me.darki.konas.event.events.RenderEnchantmentTableEvent;
import net.minecraft.client.renderer.tileentity.TileEntityEnchantmentTableRenderer;
import net.minecraft.tileentity.TileEntityEnchantmentTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityEnchantmentTableRenderer.class)
public class MixinTileEntityEnchantmentTableRenderer {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void renderBook(TileEntityEnchantmentTable te, double x, double y, double z, float partialTicks, int destroyStage, float alpha, CallbackInfo ci) {
        RenderEnchantmentTableEvent event = new RenderEnchantmentTableEvent();
        EventDispatcher.Companion.dispatch(event);
        if(event.isCancelled()) ci.cancel();
    }

}
