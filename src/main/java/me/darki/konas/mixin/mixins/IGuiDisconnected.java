package me.darki.konas.mixin.mixins;

import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiDisconnected.class)
public interface IGuiDisconnected {

    @Accessor(value = "reason")
    String getReason();

    @Accessor(value = "parentScreen")
    GuiScreen getParentScreen();

    @Accessor(value = "message")
    ITextComponent getMessage();

}
