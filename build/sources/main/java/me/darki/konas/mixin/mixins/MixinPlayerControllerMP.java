package me.darki.konas.mixin.mixins;

import cookiedragon.eventsystem.EventDispatcher;
import me.darki.konas.event.events.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerControllerMP.class)
public abstract class MixinPlayerControllerMP {

    @Shadow private int blockHitDelay;

    @Shadow private float curBlockDamageMP;

    @Shadow public abstract void syncCurrentPlayItem();

    @Shadow @Final private Minecraft mc;

    @Shadow public abstract float getBlockReachDistance();

    @Inject(method = "onStoppedUsingItem", at = @At("HEAD"), cancellable = true)
    private void onStoppedUsingItemInject(EntityPlayer playerIn, CallbackInfo ci) {
        if (playerIn.equals(Minecraft.getMinecraft().player)) {
            StopUsingItemEvent event = new StopUsingItemEvent();
            EventDispatcher.Companion.dispatch(event);
            if (event.isCancelled()) {
                if (event.isPacket()) {
                    this.syncCurrentPlayItem();
                    playerIn.stopActiveHand();
                }
                ci.cancel();
            }
        }
    }

    private boolean postDmg = false;

    @Inject(method = {"clickBlock"}, at = {@At("HEAD")}, cancellable = true)
    private void clickBlock(BlockPos posBlock, EnumFacing directionFacing, CallbackInfoReturnable<Boolean> cir) {
        LeftClickBlockEvent clickEvent = new LeftClickBlockEvent(mc.player, posBlock, directionFacing, ForgeHooks.rayTraceEyeHitVec(mc.player, getBlockReachDistance() + 1));
        EventDispatcher.Companion.dispatch(clickEvent);
        if(clickEvent.isCancelled()) {
            cir.setReturnValue(false);
        } else {
            DamageBlockEvent event = DamageBlockEvent.get(posBlock, directionFacing, blockHitDelay, curBlockDamageMP);
            blockHitDelay = event.getBlockHitDelay();
            curBlockDamageMP = event.getCurBlockDamageMP();
            EventDispatcher.Companion.dispatch(event);
            if (event.isCancelled()) {
                cir.setReturnValue(false);
                return;
            }
            postDmg = true;
        }
    }

    @Inject(method = "onPlayerDamageBlock(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z", at = @At("HEAD"), cancellable = true)
    private void onPlayerDamageBlock(BlockPos posBlock, EnumFacing directionFacing, CallbackInfoReturnable<Boolean> cir) {
        DamageBlockEvent event = DamageBlockEvent.get(posBlock, directionFacing, blockHitDelay, curBlockDamageMP);
        EventDispatcher.Companion.dispatch(event);
        blockHitDelay = event.getBlockHitDelay();
        curBlockDamageMP = event.getCurBlockDamageMP();
        if (event.isCancelled()) {
            cir.setReturnValue(false);
            return;
        }
        postDmg = true;
    }

    @Inject(method = {"clickBlock"}, at = {@At("RETURN")})
    private void clickBlockPost(BlockPos posBlock, EnumFacing directionFacing, CallbackInfoReturnable<Boolean> cir) {
        if (postDmg) {
            PostMiningEvent event = new PostMiningEvent();
            EventDispatcher.Companion.dispatch(event);
        }
    }

    @Inject(method = "onPlayerDamageBlock(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z", at = @At("RETURN"))
    private void onPlayerDamageBlockPost(BlockPos posBlock, EnumFacing directionFacing, CallbackInfoReturnable<Boolean> cir) {
        if (postDmg) {
            PostMiningEvent event = new PostMiningEvent();
            EventDispatcher.Companion.dispatch(event);
        }
    }

    @Inject(method = "getBlockReachDistance", at = @At("RETURN"), cancellable = true)
    private void onGetBlockReachDistance(CallbackInfoReturnable<Float> cir) {
        BlockReachDistanceEvent event = BlockReachDistanceEvent.get(cir.getReturnValue());
        EventDispatcher.Companion.dispatch(event);
        cir.setReturnValue(event.getReachDistance());
    }

    @Inject(method = "windowClick", at = @At("RETURN"))
    private void onWindowClick(int windowId, int slotId, int mouseButton, ClickType type, EntityPlayer player, CallbackInfoReturnable<ItemStack> cir) {
        ClickWindowEvent event = new ClickWindowEvent();
        EventDispatcher.Companion.dispatch(event);
    }

    @Inject(method = "syncCurrentPlayItem", at = @At(value = "FIELD", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;connection:Lnet/minecraft/client/network/NetHandlerPlayClient;"))
    private void onSyncPlayerItemSendPacket(CallbackInfo ci) {
        SyncPlayerItemEvent event = new SyncPlayerItemEvent();
        EventDispatcher.Companion.dispatch(event);
    }

    @Inject(method={"onPlayerDestroyBlock"}, at={@At(value="INVOKE", target="net/minecraft/block/Block.removedByPlayer(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/EntityPlayer;Z)Z")}, cancellable=true)
    private void onPlayerDestroyBlock(BlockPos blockPos, CallbackInfoReturnable<Boolean> cir) {
        PlayerDestroyBlockEvent event = new PlayerDestroyBlockEvent(blockPos);
        EventDispatcher.Companion.dispatch(event);

        if (event.isCancelled()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "processRightClickBlock", at = @At("HEAD"))
    public void rightClickBlock(EntityPlayerSP player, WorldClient worldIn, BlockPos pos, EnumFacing direction, Vec3d vec, EnumHand hand, CallbackInfoReturnable<EnumActionResult> cir) {
        ProcessRightClickBlockEvent event = new ProcessRightClickBlockEvent(player, worldIn, pos, direction, vec, hand);
        EventDispatcher.Companion.dispatch(event);

        if (event.isCancelled()) {
            event.setCancelled(true);
        }
    }
}
