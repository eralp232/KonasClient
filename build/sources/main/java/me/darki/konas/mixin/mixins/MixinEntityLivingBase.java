package me.darki.konas.mixin.mixins;

import cookiedragon.eventsystem.EventDispatcher;
import me.darki.konas.event.events.ElytraEvent;
import me.darki.konas.event.events.HandleLiquidJumpEvent;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityLivingBase.class, priority = Integer.MAX_VALUE)
public abstract class MixinEntityLivingBase extends MixinEntity {

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    public void onTravel(float strafe, float vertical, float forward, CallbackInfo ci) {
        ElytraEvent event = ElytraEvent.get((EntityLivingBase) (Object) this);
        EventDispatcher.Companion.dispatch(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method={"handleJumpWater"}, at={@At(value="HEAD")}, cancellable=true)
    private void handleJumpWater(CallbackInfo ci) {
        HandleLiquidJumpEvent event = new HandleLiquidJumpEvent();
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method={"handleJumpLava"}, at={@At(value="HEAD")}, cancellable=true)
    private void handleJumpLava(CallbackInfo ci) {
        HandleLiquidJumpEvent event = new HandleLiquidJumpEvent();
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
