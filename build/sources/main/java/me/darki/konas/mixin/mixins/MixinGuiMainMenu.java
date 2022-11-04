package me.darki.konas.mixin.mixins;

import me.darki.konas.util.KonasGlobals;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Mixin(GuiMainMenu.class)
public abstract class MixinGuiMainMenu extends GuiScreen {

    @Shadow private GuiButton realmsButton;

    @Shadow private GuiButton modButton;

    @Inject(method = "actionPerformed", at = @At(value = "RETURN"))
    public void displayAltManager(GuiButton button, CallbackInfo ci) {
        if(button.id == 8) {
            KonasGlobals.INSTANCE.altManager.setPrevScreen(this);
            Minecraft.getMinecraft().displayGuiScreen(KonasGlobals.INSTANCE.altManager);
        } else if (button.id == 69420) {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                try {
                    Desktop.getDesktop().browse(new URI("https://discord.gg/gpVZ4Y6cpq"));
                } catch (URISyntaxException | IOException ignored) {

                }
            }
        }
    }

    @Inject(method = "addSingleplayerMultiplayerButtons", at = @At("HEAD"), cancellable = true)
    public void addAllButtons(int p_73969_1_, int p_73969_2_, CallbackInfo ci) {
        IGuiScreen screen = (IGuiScreen) (GuiScreen) (Object) this;
        List<GuiButton> buttonList = screen.getButtonList();

        // Singleplayer
        buttonList.add(new GuiButton(1, ((GuiScreen) (Object) this).width / 2 - 100, p_73969_1_, 98, 20, I18n.format("menu.singleplayer")));

        // Protocol
        buttonList.add(new GuiButton(69420,  ((GuiScreen) (Object) this).width / 2 + 2, p_73969_1_, 98, 20, "Discord"));

        // Multiplayer
        buttonList.add(new GuiButton(2, ((GuiScreen) (Object) this).width / 2 - 100, p_73969_1_ + p_73969_2_, 98, 20, I18n.format("menu.multiplayer")));

        // Alt Manager
        buttonList.add(new GuiButton(8,  ((GuiScreen) (Object) this).width / 2 + 2, p_73969_1_ + p_73969_2_, 98, 20, "Alt Manager"));

        // Realms
        buttonList.add(this.realmsButton = new GuiButton(14, ((GuiScreen) (Object) this).width / 2 + 2, p_73969_1_ + p_73969_2_ * 2, 98, 20, I18n.format("menu.online").replace("Minecraft", "").trim()));

        // Mods
        buttonList.add(this.modButton = new GuiButton(6, ((GuiScreen) (Object) this).width / 2 - 100, p_73969_1_ + p_73969_2_ * 2, 98, 20, I18n.format("fml.menu.mods")));

        screen.setButtonList(buttonList);
        ci.cancel();
    }

}
