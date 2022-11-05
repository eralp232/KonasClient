package me.darki.konas.mixin.mixins;

import me.darki.konas.event.events.PlaceBlockEvent;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemBlock.class)
public abstract class MixinItemBlock {
    @Shadow protected abstract Block getBlockRaw();

    @Inject(method = "onItemUse", at = @At("INVOKE"), cancellable = true)
    public void onPlaceBlock(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ, CallbackInfoReturnable<EnumActionResult> cir) {
        PlaceBlockEvent event = new PlaceBlockEvent(player, worldIn, pos, facing, hitX, hitY, hitZ);

        if (event.isCancelled()) {
            if (!this.getBlockRaw().isReplaceable(worldIn, pos)) {
                pos = pos.offset(facing);
            }

            ItemStack itemstack = player.getHeldItem(hand);

            if (!itemstack.isEmpty() && player.canPlayerEdit(pos, facing, itemstack) && worldIn.mayPlace(this.getBlockRaw(), pos, false, facing, player)) {
                cir.setReturnValue(EnumActionResult.SUCCESS);
            } else {
                cir.setReturnValue(EnumActionResult.FAIL);
            }
        }
    }
}
