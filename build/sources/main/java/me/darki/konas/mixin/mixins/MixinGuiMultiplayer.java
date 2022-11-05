package me.darki.konas.mixin.mixins;

import me.darki.konas.gui.protocol.GuiProtocolSlider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.List;

@Mixin(GuiMultiplayer.class)
public abstract class MixinGuiMultiplayer extends GuiScreen {

    @Inject(method = "createButtons", at = @At("HEAD"), cancellable = true)
    public void addAllButtons(CallbackInfo ci) {
        IGuiScreen screen = (IGuiScreen) (GuiScreen) (Object) this;

        List<GuiButton> buttonList = screen.getButtonList();

        if (!new File(Minecraft.getMinecraft().gameDir, "novia").exists()) {
            buttonList.add(new GuiProtocolSlider(1200, this.width / 2 + 4 + 76 + 76, this.height - 28, 105, 20));
            screen.setButtonList(buttonList);
        }
    }

}