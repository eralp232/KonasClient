package me.darki.konas.mixin.mixins;

import com.mojang.authlib.GameProfile;
import cookiedragon.eventsystem.EventDispatcher;
import me.darki.konas.event.events.*;
import me.darki.konas.gui.beacon.CustomGuiBeacon;
import me.darki.konas.module.modules.misc.NoDesync;
import me.darki.konas.util.KonasGlobals;
import me.darki.konas.util.client.RotationUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.client.CPacketInput;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketVehicleMove;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityPlayerSP.class, priority = Integer.MAX_VALUE)
public abstract class MixinEntityPlayerSP extends EntityPlayer {
    @Shadow
    protected Minecraft mc;

    @Shadow @Final public NetHandlerPlayClient connection;

    @Shadow public MovementInput movementInput;

    public MixinEntityPlayerSP(World worldIn, GameProfile gameProfileIn) {
        super(worldIn, gameProfileIn);
    }

    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/AbstractClientPlayer;move(Lnet/minecraft/entity/MoverType;DDD)V"), cancellable = true)
    public void moveInject(MoverType type, double x, double y, double z, CallbackInfo ci) {
        ci.cancel();
        PlayerMoveEvent event = PlayerMoveEvent.get(type, x, y, z);
        EventDispatcher.Companion.dispatch(event);
        if(!event.isCancelled()) {
            super.move(event.getType(), event.getX(), event.getY(), event.getZ());
        }
    }

    @Inject(method = "onUpdate", at = @At("HEAD"))
    public void onPlayerUpdate(CallbackInfo info) {
        PlayerUpdateEvent playerUpdateEvent = new PlayerUpdateEvent();
        EventDispatcher.Companion.dispatch(playerUpdateEvent);
    }

    @Inject(method = "onUpdateWalkingPlayer", at = @At("TAIL"))
    public void onUpdateWalkingPlayerPost(CallbackInfo ci) {
        UpdateWalkingPlayerEvent postEvent = UpdateWalkingPlayerEvent.Post.get(mc.player.posX, mc.player.posY, mc.player.posY, mc.player.rotationYaw, mc.player.rotationPitch, mc.player.onGround);
        EventDispatcher.Companion.dispatch(postEvent);
    }

    @Redirect(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/AbstractClientPlayer;onUpdate()V"))
    public void redirectUpdateSuper(AbstractClientPlayer abstractClientPlayer) {

    }

    @Inject(method = "onUpdate", at = @At(value = "HEAD"), cancellable = true)
    public void redirectUpdateWalking(CallbackInfo ci) {
        if (this.world.isBlockLoaded(new BlockPos(this.posX, 0.0D, this.posZ))) {
            super.onUpdate();
            UpdateWalkingPlayerEvent event = UpdateWalkingPlayerEvent.Pre.get(mc.player.posX, mc.player.getEntityBoundingBox().minY, mc.player.posY, mc.player.rotationYaw, mc.player.rotationPitch, mc.player.onGround);
            NoDesync.isSpoofing = true;
            EventDispatcher.Companion.dispatch(event);
            if (KonasGlobals.INSTANCE.rotationManager.isRotationsSet()) {
                ci.cancel();
                NoDesync.spoofTimer.reset();
                if (this.isRiding()) {
                    this.connection.sendPacket(new CPacketPlayer.Rotation(KonasGlobals.INSTANCE.rotationManager.getYaw(), KonasGlobals.INSTANCE.rotationManager.getPitch(), this.onGround));
                    this.connection.sendPacket(new CPacketInput(this.moveStrafing, this.moveForward, this.movementInput.jump, this.movementInput.sneak));
                    Entity entity = this.getLowestRidingEntity();

                    if (entity != this && entity.canPassengerSteer()) {
                        this.connection.sendPacket(new CPacketVehicleMove(entity));
                    }
                } else {
                    RotationUtil.update(KonasGlobals.INSTANCE.rotationManager.getYaw(), KonasGlobals.INSTANCE.rotationManager.getPitch());
                }
                UpdateWalkingPlayerEvent postEvent = UpdateWalkingPlayerEvent.Post.get(mc.player.posX, mc.player.posY, mc.player.posY, mc.player.rotationYaw, mc.player.rotationPitch, mc.player.onGround);
                EventDispatcher.Companion.dispatch(postEvent);
            } else {
                NoDesync.isSpoofing = false;
            }
        }
        KonasGlobals.INSTANCE.rotationManager.reset();
    }

    @Inject(method = "displayGUIChest", at = @At("HEAD"), cancellable = true)
    public void onDisplayGUIChest(IInventory chestInventory, CallbackInfo ci) {
        if (chestInventory instanceof IInteractionObject && "minecraft:beacon".equals(((IInteractionObject) chestInventory).getGuiID())) {
            OpenBeaconEvent event = new OpenBeaconEvent();
            EventDispatcher.Companion.dispatch(event);
            Minecraft.getMinecraft().displayGuiScreen(new CustomGuiBeacon(this.inventory, chestInventory));
            if(event.isCancelled()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "dismountRidingEntity", at = @At("HEAD"), cancellable = true)
    public void onDismountRidingEntity(CallbackInfo ci) {
        DismountRidingEntityEvent event = DismountRidingEntityEvent.get();
        EventDispatcher.Companion.dispatch(event);
        if(event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    public void onSendChatMessage(String message, CallbackInfo ci) {
        ci.cancel();
        ChatEvent event = new ChatEvent(message);
        EventDispatcher.Companion.dispatch(event);
        if(event.isCancelled()) return;
        connection.sendPacket(new CPacketChatMessage(event.getMessage()));
    }

    @Redirect(method = "onUpdateWalkingPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;isCurrentViewEntity()Z"))
    private boolean redirectIsCurrentViewEntity(EntityPlayerSP entityPlayerSP) {
        Minecraft mc = Minecraft.getMinecraft();
        FreecamEvent event = new FreecamEvent();
        EventDispatcher.Companion.dispatch(event);
        if (event.isCancelled()) {
            return entityPlayerSP == mc.player;
        }
        return mc.getRenderViewEntity() == entityPlayerSP;
    }

    @Redirect(method = "updateEntityActionState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;isCurrentViewEntity()Z"))
    private boolean redirectIsCurrentViewEntity2(EntityPlayerSP entityPlayerSP) {
        Minecraft mc = Minecraft.getMinecraft();
        FreecamEvent event = new FreecamEvent();
        EventDispatcher.Companion.dispatch(event);
        if (event.isCancelled()) {
            return entityPlayerSP == mc.player;
        }
        return mc.getRenderViewEntity() == entityPlayerSP;
    }

}
