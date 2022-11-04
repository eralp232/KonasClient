package me.darki.konas.mixin.mixins;

import cookiedragon.eventsystem.EventDispatcher;
import me.darki.konas.event.events.WorldClientInitEvent;
import me.darki.konas.module.ModuleManager;
import net.minecraft.client.multiplayer.WorldClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldClient.class)
public class MixinWorldClient {

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void onWorldClientInit(CallbackInfo callbackInfo) {
        ModuleManager.handleWorldJoin();
        WorldClientInitEvent event = new WorldClientInitEvent();
        EventDispatcher.Companion.dispatch(event);
    }
}