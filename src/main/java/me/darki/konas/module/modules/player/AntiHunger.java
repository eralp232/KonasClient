package me.darki.konas.module.modules.player;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.PacketEvent;
import me.darki.konas.mixin.mixins.ICPacketPlayer;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;

public class AntiHunger extends Module {
    public static Setting<Boolean> sprint = new Setting<>("Sprint", true);
    public static Setting<Boolean> noGround = new Setting<>("Ground", true);

    private boolean isOnGround = false;

    public AntiHunger() {
        super("AntiHunger", "Prevents hunger loss", Category.PLAYER, "NoHunger");
    }

    public void onEnable() {
        if (sprint.getValue() && mc.player != null) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
        }
    }

    public void onDisable() {
        if (sprint.getValue() && mc.player != null && mc.player.isSprinting()) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
        }
    }

    @Subscriber
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketEntityAction) {
            CPacketEntityAction action = (CPacketEntityAction) event.getPacket();
            if (sprint.getValue() && (action.getAction() == CPacketEntityAction.Action.START_SPRINTING || action.getAction() == CPacketEntityAction.Action.STOP_SPRINTING)) {
                event.setCancelled(true);
            }
        }

        if (event.getPacket() instanceof CPacketPlayer) {
            CPacketPlayer player = (CPacketPlayer) event.getPacket();
            boolean ground = mc.player.onGround;
            if (noGround.getValue() && isOnGround && ground && player.getY(0.0) == (!((ICPacketPlayer) player).isMoving() ? 0.0 : mc.player.posY)) {
                ((ICPacketPlayer) player).setOnGround(false);
            }
            isOnGround = ground;
        }
    }
}
