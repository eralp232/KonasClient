package me.darki.konas.mixin.mixins;

import cookiedragon.eventsystem.EventDispatcher;
import me.darki.konas.event.events.BossBarEvent;
import net.minecraft.client.gui.GuiBossOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiBossOverlay.class)
public class MixinGuiBossOverlay {

    @Inject(method = "renderBossHealth", at = @At("HEAD"), cancellable = true)
    private void renderBossHealth(CallbackInfo ci) {
        BossBarEvent bossBarEvent = BossBarEvent.get();
        EventDispatcher.Companion.dispatch(bossBarEvent);
        if(bossBarEvent.isCancelled()) ci.cancel();
    }

}
