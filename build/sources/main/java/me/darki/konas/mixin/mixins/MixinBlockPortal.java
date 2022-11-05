package me.darki.konas.mixin.mixins;

import cookiedragon.eventsystem.EventDispatcher;
import me.darki.konas.event.events.GetPortalBoundingBoxEvent;
import net.minecraft.block.BlockPortal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockPortal.class)
public class MixinBlockPortal {
    private static final AxisAlignedBB EMPTY_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);

    @Inject(method = "getBoundingBox", at = @At("HEAD"), cancellable = true)
    private void injectBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos, CallbackInfoReturnable<AxisAlignedBB> cir) {
        GetPortalBoundingBoxEvent event = new GetPortalBoundingBoxEvent(state, source, pos);
        EventDispatcher.Companion.dispatch(event);
        if (event.isCancelled()) {
            cir.setReturnValue(EMPTY_AABB);
        }
    }
}