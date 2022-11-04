package me.darki.konas.mixin.mixins;

import cookiedragon.eventsystem.EventDispatcher;
import me.darki.konas.event.events.ParticleEffectEvent;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public class MixinParticleManager {

    @Inject(method = "addEffect", at = @At("HEAD"), cancellable = true)
    public void addEffect(Particle effect, CallbackInfo ci) {
        ParticleEffectEvent event = new ParticleEffectEvent(effect);
        EventDispatcher.Companion.dispatch(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

}