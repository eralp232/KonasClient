package me.darki.konas.module.modules.client;

import cookiedragon.eventsystem.Subscriber;
import io.netty.buffer.Unpooled;
import me.darki.konas.event.events.PacketEvent;
import me.darki.konas.mixin.mixins.ICPacketCustomPayload;
import me.darki.konas.module.Module;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;

public class NoForge extends Module {
    public NoForge() {
        super("NoForge", "Prevents client from sending forge signature", Category.CLIENT);
    }

    @Subscriber
    public void onPacketSend(PacketEvent.Send event) {
        if (!mc.isIntegratedServerRunning()) {
            if (event.getPacket().getClass().getName().equals("net.minecraftforge.fml.common.network.internal.FMLProxyPacket")) {
                event.setCancelled(true);
            } else if (event.getPacket() instanceof CPacketCustomPayload) {
                if (((CPacketCustomPayload) event.getPacket()).getChannelName().equalsIgnoreCase("MC|Brand")) {
                    ((ICPacketCustomPayload) event.getPacket()).setData(new PacketBuffer(Unpooled.buffer()).writeString("vanilla"));
                }
            }
        }
    }
}
