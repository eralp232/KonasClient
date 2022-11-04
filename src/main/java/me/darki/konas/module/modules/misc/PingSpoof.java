package me.darki.konas.module.modules.misc;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.PacketEvent;
import me.darki.konas.event.events.Render3DEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.timer.Timer;
import net.minecraft.network.play.client.CPacketKeepAlive;

public class PingSpoof extends Module {

    private Setting<Integer> ping = new Setting<>("Ms", 200, 2000, 0, 1);

    public PingSpoof() {
        super("PingSpoof", Category.MISC);
    }

    Timer timer = new Timer();

    CPacketKeepAlive cPacketKeepAlive = null;

    @Subscriber
    public void onPacketSend(PacketEvent.Send event) {
        if(event.getPacket() instanceof CPacketKeepAlive && cPacketKeepAlive != event.getPacket() && ping.getValue() != 0) {
            cPacketKeepAlive = (CPacketKeepAlive) event.getPacket();
            event.cancel();
            timer.reset();
        }
    }

    @Override
    public String getExtraInfo() {
        return ping.getValue() + "ms";
    }

    @Subscriber
    public void onUpdate(Render3DEvent event) {
        if(timer.hasPassed(ping.getValue()) && cPacketKeepAlive != null) {
            mc.player.connection.sendPacket(cPacketKeepAlive);
           cPacketKeepAlive = null;
        }
    }

}
