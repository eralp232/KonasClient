package me.darki.konas.event.listener;

import cookiedragon.eventsystem.EventDispatcher;
import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.PacketEvent;
import me.darki.konas.event.events.PlayerConnectEvent;
import net.minecraft.network.play.server.SPacketPlayerListItem;

public class JoinLeaveListener {

    public static JoinLeaveListener INSTANCE = new JoinLeaveListener();

    @Subscriber
    public void onPacket(PacketEvent.Receive event) {

        if (event.getPacket() instanceof SPacketPlayerListItem) {
            SPacketPlayerListItem packet = (SPacketPlayerListItem) event.getPacket();
            if (packet.getAction() == SPacketPlayerListItem.Action.ADD_PLAYER) {
                for(SPacketPlayerListItem.AddPlayerData data : packet.getEntries()) {
                    PlayerConnectEvent.Join joinEvent = new PlayerConnectEvent.Join(data.getProfile().getName(), data.getProfile().getId());
                    EventDispatcher.Companion.dispatch(joinEvent);
                }
            } else if(packet.getAction() == SPacketPlayerListItem.Action.REMOVE_PLAYER) {
                for (SPacketPlayerListItem.AddPlayerData data : packet.getEntries()) {
                    PlayerConnectEvent.Leave leaveEvent = new PlayerConnectEvent.Leave(data.getProfile().getName(), data.getProfile().getId());
                    EventDispatcher.Companion.dispatch(leaveEvent);
                }
            }
        }

    }

}
