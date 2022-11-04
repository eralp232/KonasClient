package me.darki.konas.mixin.mixins;

import cookiedragon.eventsystem.EventDispatcher;
import me.darki.konas.event.events.KeyBindingEvent;
import net.minecraft.client.settings.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyBinding.class)
public class MixinKeyBinding {

    @Shadow
    private boolean pressed;

    @Inject(method = "isKeyDown", at = @At("RETURN"), cancellable = true)
    private void isKeyDown(CallbackInfoReturnable<Boolean> isKeyDown) {
        KeyBindingEvent event = new KeyBindingEvent(isKeyDown.getReturnValue(), this.pressed);
        EventDispatcher.Companion.dispatch(event);
        isKeyDown.setReturnValue(event.isHolding());
    }

}
