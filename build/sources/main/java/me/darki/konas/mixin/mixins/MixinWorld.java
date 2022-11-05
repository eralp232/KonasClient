package me.darki.konas.mixin.mixins;

import cookiedragon.eventsystem.EventDispatcher;
import me.darki.konas.event.events.GetRainStrengthEvent;
import me.darki.konas.event.events.GetWorldTimeEvent;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public class MixinWorld {

    @Inject(method = {"getWorldTime"}, at = {@At("HEAD")}, cancellable = true)
    public void onGetWorldTime(CallbackInfoReturnable<Long> cir) {
        GetWorldTimeEvent event = EventDispatcher.Companion.dispatch(new GetWorldTimeEvent());
        if (event.isCancelled()) cir.setReturnValue(event.getWorldTime());
    }

    @Inject(method = {"getRainStrength"}, at = {@At("HEAD")}, cancellable = true)
    public void getRainStrength(float delta, CallbackInfoReturnable<Float> ci) {
        GetRainStrengthEvent event = GetRainStrengthEvent.get();
        EventDispatcher.Companion.dispatch(event);

        if (event.isCancelled()) {
            ci.cancel();
            ci.setReturnValue(0.0F);

        }
    }
}