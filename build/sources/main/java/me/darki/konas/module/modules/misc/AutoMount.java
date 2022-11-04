package me.darki.konas.module.modules.misc;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.PacketEvent;
import me.darki.konas.event.events.UpdateWalkingPlayerEvent;
import me.darki.konas.mixin.mixins.ICPacketPlayer;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.client.PlayerUtils;
import me.darki.konas.util.timer.Timer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.passive.*;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;

import java.util.Comparator;

public class AutoMount extends Module {
    private static final Setting<Boolean> rotate = new Setting<>("Rotate", true);
    private static final Setting<Boolean> bypass = new Setting<>("Bypass", false);

    private static final Setting<Integer> range = new Setting<>("Range", 4, 10, 1, 1);
    private static final Setting<Float> delay = new Setting<>("Delay", 1F, 10F, 0F, 0.1F);

    private static final Setting<Boolean> boats = new Setting<>("Boats", false);
    private static final Setting<Boolean> horses = new Setting<>("Horses", false);
    private static final Setting<Boolean> skeletonHorses = new Setting<>("SkeletonHorses", false);
    private static final Setting<Boolean> donkeys = new Setting<>("Donkeys", true);
    private static final Setting<Boolean> pigs = new Setting<>("Pigs", false);
    private static final Setting<Boolean> llamas = new Setting<>("Llamas", false);

    public AutoMount() {
        super("AutoMount", Category.MISC);
    }

    private Timer timer = new Timer();

    private Timer angleInactivityTimer = new Timer();
    private float yaw = 0F;
    private float pitch = 0F;

    Entity selectedEntity = null;

    @Subscriber(priority = 10)
    public void onUpdateWalkingPlayerPre(UpdateWalkingPlayerEvent.Pre event) {
        if (mc.player.isRiding()) {
            return;
        }

        if (!timer.hasPassed(delay.getValue() * 1000)) {
            return;
        }

        timer.reset();

        selectedEntity = mc.world.loadedEntityList.stream()
                .filter(entity -> isValidEntity(entity))
                .min(Comparator.comparing(entity -> mc.player.getDistance(entity)))
                .orElse(null);

        if (rotate.getValue() && selectedEntity != null) {
            double[] v = PlayerUtils.calculateLookAt(selectedEntity.posX, selectedEntity.posY, selectedEntity.posZ, mc.player);
            yaw = (float) v[0];
            pitch = (float) v[1];
            angleInactivityTimer.reset();
        }
    }

    @Subscriber
    public void onWalkingUpdatePost(UpdateWalkingPlayerEvent.Post event) {
        if (selectedEntity != null) {
            mc.playerController.interactWithEntity(mc.player, selectedEntity, EnumHand.MAIN_HAND);
            selectedEntity = null;
        }
    }

    @Subscriber
    private void onPacket(PacketEvent.Send event) {
        if (mc.world == null || mc.player == null) return;
        if (event.getPacket() instanceof CPacketPlayer
                && !angleInactivityTimer.hasPassed(350)) {
            CPacketPlayer packet = (CPacketPlayer) event.getPacket();
            if (event.getPacket() instanceof CPacketPlayer.Position) {
                event.setCancelled(true);
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(packet.getX(mc.player.posX), packet.getY(mc.player.posY), packet.getZ(mc.player.posZ), (float) yaw, (float) pitch, packet.isOnGround()));
            } else {
                ((ICPacketPlayer) packet).setYaw((float) yaw);
                ((ICPacketPlayer) packet).setPitch((float) pitch);
            }
        }

        if (bypass.getValue() && event.getPacket() instanceof CPacketUseEntity) {
            CPacketUseEntity packet = (CPacketUseEntity) event.getPacket();

            if (packet.getEntityFromWorld(mc.world) instanceof AbstractChestHorse) {
                if (packet.getAction() == CPacketUseEntity.Action.INTERACT_AT) {
                    event.cancel();
                }
            }
        }
    }

    private boolean isValidEntity(Entity entity) {
        if (entity.getDistance(mc.player) > range.getValue()) {
            return false;
        }

        if (entity instanceof AbstractHorse) {
            AbstractHorse horse = (AbstractHorse) entity;

            if (horse.isChild())
                return false;
        }

        if (entity instanceof EntityBoat && boats.getValue()) {
            return true;
        }

        if (entity instanceof EntitySkeletonHorse && skeletonHorses.getValue()) {
            return true;
        }

        if (entity instanceof EntityHorse && horses.getValue()) {
            return true;
        }

        if (entity instanceof EntityDonkey && donkeys.getValue()) {
            return true;
        }

        if (entity instanceof EntityPig && pigs.getValue()) {
            EntityPig pig = (EntityPig) entity;

            return pig.getSaddled();
        }

        if (entity instanceof EntityLlama && llamas.getValue()) {
            EntityLlama llama = (EntityLlama) entity;

            return !llama.isChild();
        }

        return false;
    }
}
