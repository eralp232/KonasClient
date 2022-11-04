package me.darki.konas.module.modules.player;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.PacketEvent;
import me.darki.konas.mixin.mixins.ISPacketPlayerPosLook;
import me.darki.konas.module.Module;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.network.play.server.SPacketPlayerPosLook;

public class NoRotate extends Module {
    public NoRotate() {
        super("NoRotate", "Cancels server to client rotations", Category.PLAYER);
    }

    @Subscriber
    public void onReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketPlayerPosLook) {
            if (!(mc.currentScreen instanceof GuiDownloadTerrain)) {
                SPacketPlayerPosLook packet = (SPacketPlayerPosLook) event.getPacket();
                ((ISPacketPlayerPosLook) packet).setYaw(mc.player.rotationYaw);
                ((ISPacketPlayerPosLook) packet).setPitch(mc.player.rotationPitch);
                packet.getFlags().remove(SPacketPlayerPosLook.EnumFlags.X_ROT);
                packet.getFlags().remove(SPacketPlayerPosLook.EnumFlags.Y_ROT);
            }
        }

    }
}
