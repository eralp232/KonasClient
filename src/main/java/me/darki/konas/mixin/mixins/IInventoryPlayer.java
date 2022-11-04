package me.darki.konas.mixin.mixins;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(InventoryPlayer.class)
public interface IInventoryPlayer {

    @Accessor(value = "armorInventory")
    void setArmorInventory(NonNullList<ItemStack> armorInventory);

    @Accessor(value = "mainInventory")
    void setMainInventory(NonNullList<ItemStack> mainInventory);

    @Accessor(value = "allInventories")
    List<NonNullList<ItemStack>> getAllInventories();

}
