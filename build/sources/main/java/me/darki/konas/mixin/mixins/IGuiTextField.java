package me.darki.konas.mixin.mixins;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiTextField.class)
public interface IGuiTextField {
    @Accessor(value = "fontRenderer")
    FontRenderer getFontRenderer();

    @Accessor(value = "maxStringLength")
    void setMaxStringLength(int length);
}
