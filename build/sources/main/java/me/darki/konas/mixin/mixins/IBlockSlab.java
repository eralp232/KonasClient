package me.darki.konas.mixin.mixins;

import net.minecraft.block.BlockSlab;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockSlab.class)
public interface IBlockSlab {

    @Accessor(value = "AABB_BOTTOM_HALF")
    AxisAlignedBB getAABBBOTTOMHALF();

    @Accessor(value = "AABB_TOP_HALF")
    AxisAlignedBB getAABBTOPHALF();

}
