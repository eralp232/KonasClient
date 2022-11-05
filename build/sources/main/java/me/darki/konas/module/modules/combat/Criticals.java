package me.darki.konas.module.modules.combat;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.PacketEvent;
import me.darki.konas.event.events.UpdateEvent;
import me.darki.konas.module.Module;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.setting.Setting;
import net.minecraft.block.BlockWeb;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.util.LinkedList;
import java.util.Queue;

public class Criticals extends Module {


    private final Setting<Mode> mode = new Setting<>("Mode", Mode.PACKET);
    private final Setting<Boolean> webCrits = new Setting<>("WebCrits", false).withVisibility(() -> mode.getValue() == Mode.BYPASS);
    private final Setting<Boolean> vehicles = new Setting<>("Vehicles", false);
    private final Setting<Integer> hits = new Setting<>("Hits", 3, 15, 0, 1).withVisibility(vehicles::getValue);
    private final Setting<Integer> delay = new Setting<>("Delay", 1, 10, 1, 1);
    private final Setting<Boolean> onlyWhenKA = new Setting<>("OnlyWhenKA", true);

    private final Queue<CPacketUseEntity> vehicleHitQueue = new LinkedList<>();

    private CPacketUseEntity delayedPacket = null;
    private CPacketAnimation delayedAnimation = null;
    private int awaitingPackets = 0;

    public Criticals() {
        super("Criticals", Keyboard.KEY_NONE, Category.COMBAT, "Crits", "AlwaysCrit");
    }

    private enum Mode {
        PACKET, BYPASS, JUMP, SMALLJUMP
    }

    public void onEnable() {
        delayedPacket = null;
        delayedAnimation = null;
    }

    int tick = 0;

    @Subscriber
    public void onUpdate(UpdateEvent event) {
        if (mc.player == null || mc.world == null) return;
        if(event.getPhase() == TickEvent.Phase.START) {
            if(!vehicleHitQueue.isEmpty() && tick % delay.getValue() == 0) {
                mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
                mc.player.connection.sendPacket(vehicleHitQueue.poll());
            }
            tick++;
        }
        if (mc.player.motionY < 0 && delayedPacket != null && delayedAnimation != null && (mode.getValue() == Mode.JUMP || mode.getValue() == Mode.SMALLJUMP)) {
            mc.player.connection.sendPacket(delayedPacket);
            mc.player.connection.sendPacket(delayedAnimation);
            delayedPacket = null;
            delayedAnimation = null;
        }
    }

    @Override
    public String getExtraInfo() {
        return mode.getValue().toString().charAt(0) + mode.getValue().toString().substring(1).toLowerCase();
    }

    @Subscriber
    public void onPacket(PacketEvent.Send event) {
        if (mc.player == null || mc.world == null) return;
        if(!ModuleManager.getModuleByName("KillAura").isEnabled() && onlyWhenKA.getValue()) return;
        if (mode.getValue() == Mode.JUMP || mode.getValue() == Mode.SMALLJUMP) {
            if (delayedPacket != null && delayedAnimation != null) {
                return;
            }
        }
        if (event.getPacket() instanceof CPacketUseEntity && ((CPacketUseEntity) event.getPacket()).getAction() == CPacketUseEntity.Action.ATTACK && mc.player.onGround && mc.player.collidedVertically && !mc.player.isInLava() && !mc.player.isInWater()) {
            Entity attackedEntity = ((CPacketUseEntity) event.getPacket()).getEntityFromWorld(mc.world);
            if(attackedEntity instanceof EntityEnderCrystal || attackedEntity == null) return;

            if((attackedEntity instanceof EntityMinecart || attackedEntity instanceof EntityBoat) && vehicles.getValue()) {
                if(awaitingPackets > 0) {
                    awaitingPackets--;
                    return;
                }
                awaitingPackets = hits.getValue();
                for(int i = 0; i < hits.getValue(); i++) {
                    vehicleHitQueue.add(new CPacketUseEntity(attackedEntity));
                }
                return;
            }

            switch (mode.getValue()) {
                case PACKET:
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.0625101D, mc.player.posZ, false));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.0125D, mc.player.posZ, false));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
                    break;
                case BYPASS:
                    if (webCrits.getValue()) {
                        if (mc.world.getBlockState(new BlockPos(mc.player)).getBlock() instanceof BlockWeb) {
                            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.0625101D, mc.player.posZ, false));
                            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
                            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.0125D, mc.player.posZ, false));
                            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
                            break;
                        }
                    }
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.11D, mc.player.posZ, false));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.1100013579D, mc.player.posZ, false));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.0000013579D, mc.player.posZ, false));
                    break;
                case JUMP:
                    if (delayedPacket == null) {
                        mc.player.jump();
                        delayedPacket = (CPacketUseEntity) event.getPacket();
                        event.setCancelled(true);
                    }
                    break;
                case SMALLJUMP:
                    if (delayedPacket == null) {
                        mc.player.jump();
                        mc.player.motionY = 0.25;
                        delayedPacket = (CPacketUseEntity) event.getPacket();
                        event.setCancelled(true);
                    }
                    break;
            }
        } else if (event.getPacket() instanceof CPacketAnimation && mc.player.onGround && mc.player.collidedVertically && !mc.player.isInLava() && !mc.player.isInWater() && delayedPacket != null && delayedAnimation == null) {
            delayedAnimation = (CPacketAnimation) event.getPacket();
        }
    }
}
