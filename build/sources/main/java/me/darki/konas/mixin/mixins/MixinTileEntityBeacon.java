package me.darki.konas.mixin.mixins;

import cookiedragon.eventsystem.EventDispatcher;
import me.darki.konas.event.events.RenderBeaconBeamEvent;
import net.minecraft.tileentity.TileEntityBeacon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TileEntityBeacon.class)
public class MixinTileEntityBeacon {

    @Inject(method = "shouldBeamRender", at = @At("HEAD"), cancellable = true)
    public void injectShouldBeamRender(CallbackInfoReturnable<Float> cir) {
        RenderBeaconBeamEvent event = new RenderBeaconBeamEvent();
        EventDispatcher.Companion.dispatch(event);
        if(event.isCancelled()) cir.setReturnValue(0.0F);
    }

}
