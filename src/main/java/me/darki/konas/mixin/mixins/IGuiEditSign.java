package me.darki.konas.mixin.mixins;

import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.tileentity.TileEntitySign;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiEditSign.class)
public interface IGuiEditSign {

    @Accessor(value = "tileSign")
    TileEntitySign getTileEntitySign();

}
