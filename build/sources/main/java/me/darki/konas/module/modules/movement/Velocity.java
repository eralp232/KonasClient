package me.darki.konas.module.modules.movement;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.*;
import me.darki.konas.mixin.mixins.ISPacketEntityVelocity;
import me.darki.konas.mixin.mixins.ISPacketExplosion;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;

public class Velocity extends Module {


    private static Setting<Float> horizontal = new Setting<>("Horizontal", 0f, 10f, 0f, 1f);
    private static Setting<Float> vertical = new Setting<>("Vertical", 0f, 10f, 0f, 1f);
    private static Setting<Boolean> noPush = new Setting<>("NoPush", true);
    private static Setting<Boolean> noHook = new Setting<>("NoHook", true);
    private static Setting<Boolean> noPiston = new Setting<>("NoPiston", false);

    public Velocity() {
        super("Velocity", Category.MOVEMENT, "AntiKnockback");
    }

    @Subscriber
    public void onPlayerPushedByWater(PlayerPushedByWaterEvent event) {
        if (noPush.getValue()) {
            event.setCancelled(true);
        }
    }

    @Subscriber
    public void onPushOutOfBlocks(BlockPushOutEvent event) {
        if (noPush.getValue()) {
            event.setCancelled(true);
        }
    }

    @Subscriber
    private void onVelocity(PacketEvent.Receive event) {
        if (mc.world == null || mc.player == null) return;
        if (event.getPacket() instanceof SPacketEntityVelocity) {
            SPacketEntityVelocity velocity = (SPacketEntityVelocity) event.getPacket();
            if (velocity.getEntityID() == mc.player.getEntityId()) {
                if (horizontal.getValue() == 0 && vertical.getValue() == 0) {
                    event.setCancelled(true);
                } else {
                    ((ISPacketEntityVelocity) velocity).setMotionX((int) (velocity.getMotionX() * horizontal.getValue()));
                    ((ISPacketEntityVelocity) velocity).setMotionY((int) (velocity.getMotionY() * vertical.getValue()));
                    ((ISPacketEntityVelocity) velocity).setMotionZ((int) (velocity.getMotionZ() * horizontal.getValue()));
                }
            }
        } else if (event.getPacket() instanceof SPacketExplosion) {
            SPacketExplosion velocity = (SPacketExplosion) event.getPacket();
            ((ISPacketExplosion) velocity).setMotionX((int) (velocity.getMotionX() * horizontal.getValue()));
            ((ISPacketExplosion) velocity).setMotionY((int) (velocity.getMotionY() * vertical.getValue()));
            ((ISPacketExplosion) velocity).setMotionZ((int) (velocity.getMotionZ() * horizontal.getValue()));
        } else if (event.getPacket() instanceof SPacketEntityStatus && noHook.getValue()) {
            final SPacketEntityStatus packet = (SPacketEntityStatus) event.getPacket();
            if (packet.getOpCode() == 31) {
               Entity entity = packet.getEntity(mc.world);
                if (entity != null && entity instanceof EntityFishHook) {
                    EntityFishHook fishHook = (EntityFishHook) entity;
                    if (fishHook.caughtEntity == mc.player) {
                        event.cancel();
                    }
                }
            }
        }
    }

    @Subscriber
    private void onPush(EntityCollisionEvent event) {
        if (mc.world == null || mc.player == null) return;
        if (noPush.getValue() && event.getEntity() == mc.player) {
            if (event.getType() == EntityCollisionEvent.Type.HORIZONTAL) {
                event.setCoordinate(event.getCoordinate() * horizontal.getValue());
            } else if (event.getType() == EntityCollisionEvent.Type.VERTICAL) {
                event.setCoordinate(event.getCoordinate() * vertical.getValue());
            }
        }
    }

    @Subscriber
    public void onPlayerMove(PlayerMoveEvent event) {
        if ((event.getType() == MoverType.PISTON || event.getType() == MoverType.SHULKER_BOX) && noPiston.getValue()) {
            event.setCancelled(true);
        }
    }
}
