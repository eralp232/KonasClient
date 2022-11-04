package me.darki.konas.mixin.mixins;

import cookiedragon.eventsystem.EventDispatcher;
import me.darki.konas.event.events.CollisionBoxEvent;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.module.modules.render.XRay;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.List;

import static net.minecraft.block.Block.NULL_AABB;

@Mixin(value = Block.class, priority = Integer.MAX_VALUE)
public class MixinBlock {

    private CollisionBoxEvent bbEvent;

    @Inject(method = "addCollisionBoxToList(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;Z)V", at = @At("HEAD"))
    private void in(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entity, boolean isActualState, CallbackInfo ci) {
        if(Minecraft.getMinecraft().player == null || entity == null || world == null || entityBox == null) {
            return;
        }

        Block block = (Block) (Object) this;
        CollisionBoxEvent event = CollisionBoxEvent.get(block, pos, block.getCollisionBoundingBox(state, world, pos), collidingBoxes, entity);
        bbEvent = event;
        EventDispatcher.Companion.dispatch(event);

    }

    @Redirect(method = "addCollisionBoxToList(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;Z)V",
    at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;addCollisionBoxToList(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/util/math/AxisAlignedBB;)V"))
    private void addCollisionBoxToList(BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable AxisAlignedBB rawBlockBox) {
        AxisAlignedBB blockBox = (bbEvent == null) ? rawBlockBox : bbEvent.getBoundingBox();
        bbEvent = null;

        if (blockBox != null && blockBox != NULL_AABB)
        {
            AxisAlignedBB axisalignedbb = blockBox.offset(pos);

            if (entityBox.intersects(axisalignedbb))
            {
                collidingBoxes.add(axisalignedbb);
            }
        }
    }

    @Inject(method={"isFullCube"}, at={@At(value="HEAD")}, cancellable=true)
    public void isFullCubeHook(IBlockState blockState, CallbackInfoReturnable<Boolean> info) {
        if (ModuleManager.getModuleByClass(XRay.class) == null) return;
        if (ModuleManager.getModuleByClass(XRay.class).isEnabled()) {
            info.setReturnValue(XRay.blocks.getValue().getBlocks().contains((Block)Block.class.cast(this)));
            info.cancel();
        }
    }
}
