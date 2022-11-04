package me.darki.konas.mixin.mixins;

import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ItemRenderer.class, priority = Integer.MAX_VALUE)
public interface IItemRenderer {
    @Accessor(value = "equippedProgressMainHand")
    void settEquippedProgressMainHand(float equippedProgressMainHand);

    @Accessor(value = "equippedProgressMainHand")
    float gettEquippedProgressMainHand();

    @Accessor(value = "equippedProgressOffHand")
    void settEquippedProgressOffHand(float equippedProgressOffHand);

    @Accessor(value = "equippedProgressOffHand")
    float gettEquippedProgressOffHand();

    @Accessor(value = "prevEquippedProgressMainHand")
    void settPrevEquippedProgressMainHand(float prevEquippedProgressMainHand);

    @Accessor(value = "prevEquippedProgressMainHand")
    float gettPrevEquippedProgressMainHand();

    @Accessor(value = "prevEquippedProgressOffHand")
    void settPrevEquippedProgressOffHand(float prevEquippedProgressOffHand);

    @Accessor(value = "prevEquippedProgressOffHand")
    float gettPrevEquippedProgressOffHand();

    @Accessor(value = "itemStackMainHand")
    void settItemStackMainHand(ItemStack itemStackMainHand);

    @Accessor(value = "itemStackMainHand")
    ItemStack gettItemStackMainHand();

    @Accessor(value = "itemStackOffHand")
    void settItemStackOffHand(ItemStack itemStackOffHand);

    @Accessor(value = "itemStackOffHand")
    ItemStack gettItemStackOffHand();
}
