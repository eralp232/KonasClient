package me.darki.konas.mixin.mixins;

import cookiedragon.eventsystem.EventDispatcher;
import me.darki.konas.event.events.EntitySteerEvent;
import me.darki.konas.event.events.HorseSaddledEvent;
import net.minecraft.entity.passive.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractHorse.class)
public class MixinAbstractHorse {

    @Inject(method = "canBeSteered", at = @At("HEAD"), cancellable = true)
    public void canBeSteered(CallbackInfoReturnable<Boolean> ci) {

        EntitySteerEvent entitySteerEvent = EntitySteerEvent.get((AbstractHorse)(Object)this);
        EventDispatcher.Companion.dispatch(entitySteerEvent);

        if(entitySteerEvent.isCancelled()) {
            ci.setReturnValue(true);
        }

    }

    @Inject(method = "isHorseSaddled", at = @At("HEAD"), cancellable = true)
    public void isHorseSaddled(CallbackInfoReturnable<Boolean> ci) {

        HorseSaddledEvent horseSaddledEvent = new HorseSaddledEvent((AbstractHorse)(Object)this);
        EventDispatcher.Companion.dispatch(horseSaddledEvent);

        if(horseSaddledEvent.isCancelled()) {
            ci.setReturnValue(true);
        }

    }


}
