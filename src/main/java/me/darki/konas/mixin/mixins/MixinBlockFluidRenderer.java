package me.darki.konas.mixin.mixins;

import cookiedragon.eventsystem.EventDispatcher;
import me.darki.konas.event.events.XRayBlockEvent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockFluidRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={BlockFluidRenderer.class})
public class MixinBlockFluidRenderer {
    @Inject(method={"renderFluid"}, at={@At(value="HEAD")}, cancellable=true)
    public void renderFluidHook(IBlockAccess blockAccess, IBlockState blockState, BlockPos blockPos, BufferBuilder bufferBuilder, CallbackInfoReturnable<Boolean> info) {
        XRayBlockEvent event = new XRayBlockEvent(blockState.getBlock());
        EventDispatcher.Companion.dispatch(event);
        if (event.isCancelled()) {
            info.setReturnValue(false);
            info.cancel();
        }
    }
}