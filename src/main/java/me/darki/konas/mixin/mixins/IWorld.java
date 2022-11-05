package me.darki.konas.mixin.mixins;

import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(World.class)
public interface IWorld {
    @Accessor(value = "rainingStrength")
    float getRainingStrength();

    @Accessor(value = "thunderingStrength")
    float getThunderingStrength();
}
