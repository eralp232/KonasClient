package me.darki.konas.mixin.mixins;

import cookiedragon.eventsystem.EventDispatcher;
import me.darki.konas.event.events.LocateCapeEvent;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(AbstractClientPlayer.class)
public abstract class MixinAbstractClientPlayer {

    @Shadow
    @Nullable
    protected abstract NetworkPlayerInfo getPlayerInfo();

    @Inject(method = "getLocationCape", at = @At("HEAD"), cancellable = true)
    public void preGetLocationCape(CallbackInfoReturnable<ResourceLocation> callbackInfoReturnable) {
        NetworkPlayerInfo info = this.getPlayerInfo();
        if (info != null) {
            LocateCapeEvent event = new LocateCapeEvent(info.getGameProfile().getName());
            EventDispatcher.Companion.dispatch(event);
            if (event.isCancelled()) callbackInfoReturnable.setReturnValue(event.getResourceLocation());
        }
    }
}
