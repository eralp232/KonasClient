package me.darki.konas.mixin.mixins;

import net.minecraft.client.gui.ChatLine;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChatLine.class)
public interface IChatLine {

    @Accessor("lineString")
    void setLineString(ITextComponent iTextComponent);

}
