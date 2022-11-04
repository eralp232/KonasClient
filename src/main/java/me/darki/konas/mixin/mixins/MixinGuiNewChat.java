package me.darki.konas.mixin.mixins;

import cookiedragon.eventsystem.EventDispatcher;
import me.darki.konas.event.events.ChatRectEvent;
import me.darki.konas.event.events.GetChatHeightEvent;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GuiNewChat.class, priority = 100005)
public class MixinGuiNewChat {

    @Redirect(method = "drawChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiNewChat;drawRect(IIIII)V"))
    private void drawRectBackgroundClean(int left, int top, int right, int bottom, int color) {
        ChatRectEvent event = new ChatRectEvent();
        EventDispatcher.Companion.dispatch(event);
        if(!event.isCancelled()) {
            Gui.drawRect(left, top, right, bottom, color);
        }
    }

    @Inject(method = "calculateChatboxHeight", at = @At(value = "HEAD"), cancellable = true)
    private static void onGetChatHeight(float scale, CallbackInfoReturnable<Integer> cir) {
        GetChatHeightEvent event = new GetChatHeightEvent(MathHelper.floor(scale * 160.0F + 20.0F));
        EventDispatcher.Companion.dispatch(event);
        cir.setReturnValue(event.getHeight());
    }


}
