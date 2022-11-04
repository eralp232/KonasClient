package me.darki.konas.mixin.mixins;

import cookiedragon.eventsystem.EventDispatcher;
import me.darki.konas.event.events.XRayBlockEvent;
import me.darki.konas.event.events.XRayEvent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={BlockModelRenderer.class})
public class MixinBlockModelRenderer {
    @Inject(method={"renderModel(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/renderer/block/model/IBakedModel;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/BufferBuilder;Z)Z"}, at={@At(value="HEAD")}, cancellable=true)
    private void renderModelHook(IBlockAccess blockAccess, IBakedModel bakedModel, IBlockState blockState, BlockPos blockPos, BufferBuilder bufferBuilder, boolean checkSides, CallbackInfoReturnable<Boolean> info) {
        XRayBlockEvent event = new XRayBlockEvent(blockState.getBlock());
        EventDispatcher.Companion.dispatch(event);
        if (event.isCancelled()) {
            info.setReturnValue(false);
            info.cancel();
        }
    }

    @ModifyArg(method={"renderModel(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/renderer/block/model/IBakedModel;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/BufferBuilder;ZJ)Z"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/renderer/BlockModelRenderer;renderModelFlat(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/renderer/block/model/IBakedModel;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/BufferBuilder;ZJ)Z"))
    private boolean renderModelFlatHook(boolean input) {
        XRayEvent event = new XRayEvent();
        EventDispatcher.Companion.dispatch(event);
        if (event.isCancelled()) {
            return false;
        }
        return input;
    }

    @ModifyArg(method={"renderModel(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/renderer/block/model/IBakedModel;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/BufferBuilder;ZJ)Z"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/renderer/BlockModelRenderer;renderModelSmooth(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/renderer/block/model/IBakedModel;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/BufferBuilder;ZJ)Z"))
    private boolean renderModelSmoothHook(boolean input) {
        XRayEvent event = new XRayEvent();
        EventDispatcher.Companion.dispatch(event);
        if (event.isCancelled()) {
            return false;
        }
        return input;
    }
}
