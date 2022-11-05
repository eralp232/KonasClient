package me.darki.konas.module.modules.render;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.PacketEvent;
import me.darki.konas.module.Module;

public class PacketRender extends Module {
    private static float yaw = 0;
    private static float pitch = 0;

    public PacketRender() {
        super("PacketRender", Category.RENDER);
    }

    @Subscriber
    public void onPacketSend(PacketEvent.Send event) {

    }

    public static float getYaw() {
        return yaw;
    }

    public static float getPitch() {
        return pitch;
    }

    public static void setYaw(float yaw) {
        PacketRender.yaw = yaw;
    }

    public static void setPitch(float pitch) {
        PacketRender.pitch = pitch;
    }
}
