package me.darki.konas.mixin.mixins;

import cookiedragon.eventsystem.EventDispatcher;
import me.darki.konas.event.events.RenderItemToolTipEvent;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiScreen.class)
public class MixinGuiScreen {
    @Inject(method = "renderToolTip", at = @At("HEAD"), cancellable = true)
    private void toolTipHook(ItemStack itemStack, int x, int y, CallbackInfo ci) {
        final RenderItemToolTipEvent event = new RenderItemToolTipEvent(itemStack, x, y);
        EventDispatcher.Companion.dispatch(event);

        if (event.isCancelled()) ci.cancel();
    }
}
