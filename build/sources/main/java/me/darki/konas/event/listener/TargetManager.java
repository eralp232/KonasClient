package me.darki.konas.event.listener;

import cookiedragon.eventsystem.EventDispatcher;
import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.*;
import me.darki.konas.util.client.FakePlayerManager;
import me.darki.konas.util.timer.Timer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class TargetManager {
    private Map<Entity, Long> targets = new ConcurrentHashMap<>();

    public static HashMap<String, Integer> popList = new HashMap<>();

    private Timer timer = new Timer();

    public void addTarget(Entity target) {
        targets.put(target, System.currentTimeMillis());
    }

    public void removeTarget(Entity target) {
        targets.remove(target);
    }

    public Set<Entity> getTargets() {
        return targets.keySet();
    }

    public boolean isTarget(Entity suspect) {
        if (targets.containsKey(suspect)) {
            return true;
        }
        return false;
    }

    // We can use this for cool  tracers and esp
    public int getTargetLifespanColor(Entity entity) {
        try {
            if (!targets.containsKey(entity)) return 255;
            int targetColor = (int) (System.currentTimeMillis() - targets.get(entity)) / 118;
            return Math.min(targetColor, 255); // targets are only updated ever 10 sec so we have to check
        } catch (NullPointerException npe) {
            return 255;
        }
    }

    // Totally not pedo
    public Entity getYoungestTarget() {
        if(!targets.isEmpty()) {
            return Collections.max(targets.entrySet(), Map.Entry.comparingByValue()).getKey();
        }
        return null;
    }

    public void refreshTargets() {
        targets.forEach((entity, time) -> {
            if (System.currentTimeMillis() - time > TimeUnit.SECONDS.toMillis(30L)) {
                targets.remove(entity);
            }
        });
    }

    public void resetTargets() {
        targets.clear();
    }

    @Subscriber
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (timer.hasPassed(10000L)) {
            refreshTargets();
            timer.reset();
        }
    }

    @Subscriber
    public void onPacket(PacketEvent.Send event) {
        if (Minecraft.getMinecraft().world == null || Minecraft.getMinecraft().player == null) return;

        if (event.getPacket() instanceof CPacketUseEntity) {
            CPacketUseEntity packet = (CPacketUseEntity) event.getPacket();

            if (packet.getAction().equals(CPacketUseEntity.Action.ATTACK) && packet.getEntityFromWorld(Minecraft.getMinecraft().world) instanceof EntityPlayer) {
                EntityPlayer attackedEntity = (EntityPlayer) packet.getEntityFromWorld(Minecraft.getMinecraft().world);
                assert attackedEntity != null;
                addTarget(attackedEntity);
            }
        }
    }

    @Subscriber
    public void onPacket(PacketEvent.Receive event) {

        if (Minecraft.getMinecraft().world == null || Minecraft.getMinecraft().player == null) return;

        if (event.getPacket() instanceof SPacketEntityStatus) {
            SPacketEntityStatus packet = (SPacketEntityStatus) event.getPacket();
            if (packet.getOpCode() == 35) {
                Entity entity = packet.getEntity(Minecraft.getMinecraft().world);
                if (popList == null) {
                    popList = new HashMap<>();
                }

                if (popList.get(entity.getName()) == null) {
                    popList.put(entity.getName(), 1);
                } else if (popList.get(entity.getName()) != null) {
                    popList.put(entity.getName(),  popList.get(entity.getName()) + 1);
                }

                TotemPopEvent totemPopEvent = new TotemPopEvent(entity, popList.get(entity.getName()));
                EventDispatcher.Companion.dispatch(totemPopEvent);

            }
        }

    }

    @Subscriber
    public void onUpdate(UpdateEvent event) {
        if (Minecraft.getMinecraft().world == null || Minecraft.getMinecraft().player == null || event.getPhase() == TickEvent.Phase.START) {
            return;
        }

        for (EntityPlayer player : Minecraft.getMinecraft().world.playerEntities) {
            if (FakePlayerManager.isFake(player)) continue;
            if (player.getHealth() <= 0
                    && popList.containsKey(player.getName())) {
                TargetManager.popList.remove(player.getName(), TargetManager.popList.get(player.getName()));
            }
        }

        getTargets().forEach(target -> {
            if (target instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) target;
                if (player.getHealth() <= 0) {
                    TargetKillEvent targetKillEvent = new TargetKillEvent(player);
                    EventDispatcher.Companion.dispatch(targetKillEvent);
                    removeTarget(player);
                }
            }
        });

    }


}
