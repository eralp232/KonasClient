package me.darki.konas.mixin.mixins;

import cookiedragon.eventsystem.EventDispatcher;
import me.darki.konas.event.events.AddCollisionToBoxListEvent;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(value = BlockStateContainer.StateImplementation.class, priority = 2147483647)
public class MixinBlockStateContainerStateImplementation {
    // We have to use this cause old thingy is depricated
    @Shadow
    @Final
    private Block block;

    @Redirect(method = "addCollisionBoxToList", at = @At(value="INVOKE", target = "Lnet/minecraft/block/Block;addCollisionBoxToList(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;Z)V"))
    public void addCollisionBoxToList(Block blk, IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        AddCollisionToBoxListEvent event = AddCollisionToBoxListEvent.get(blk, state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
        EventDispatcher.Companion.dispatch(event);
        if (!event.isCancelled())
            block.addCollisionBoxToList(event.getState(), event.getWorld(), event.getPos(), event.getEntityBox(), event.getCollidingBoxes(), event.getEntity(), event.isActualState());
    }
}
