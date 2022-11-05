    package me.darki.konas.mixin.mixins;

    import net.minecraft.client.gui.inventory.GuiBrewingStand;
    import net.minecraft.inventory.IInventory;
    import org.spongepowered.asm.mixin.Mixin;
    import org.spongepowered.asm.mixin.gen.Accessor;

    @Mixin(GuiBrewingStand.class)
    public interface IGuiBrewingStand {

        @Accessor(value = "tileBrewingStand")
        IInventory getTileBrewingStand();

    }
