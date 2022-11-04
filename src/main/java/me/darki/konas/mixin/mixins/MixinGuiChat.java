package me.darki.konas.mixin.mixins;

import me.darki.konas.command.Command;
import me.darki.konas.gui.chat.KonasGuiChat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiTextField;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiChat.class)
public class MixinGuiChat {

    @Shadow protected GuiTextField inputField;

    @Inject(method = "keyTyped", at = @At("RETURN"))
    public void konasChatInject(char charTyped, int keyCode, CallbackInfo ci) {

        if(!(Minecraft.getMinecraft().currentScreen instanceof GuiChat) || Minecraft.getMinecraft().currentScreen instanceof KonasGuiChat) return;

        if(inputField.getText().replaceAll(" ", "").startsWith(Command.getPrefix())) {
            Minecraft.getMinecraft().displayGuiScreen(new KonasGuiChat(inputField.getText()));
        }

    }

}
