package me.darki.konas.event.listener;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.config.Config;
import me.darki.konas.event.events.DirectMessageEvent;
import me.darki.konas.event.events.LoadGuiEvent;
import me.darki.konas.event.events.UpdateEvent;
import me.darki.konas.module.modules.misc.ExtraChat;
import me.darki.konas.util.timer.Timer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraftforge.event.world.WorldEvent;

import java.util.LinkedList;
import java.util.Queue;

public class BackupListener {

    public static BackupListener INSTANCE = new BackupListener();

    public static final Queue<ExtraChat.PartyMessage> msgQueue = new LinkedList<>();
    public static final Timer messageTimer = new Timer();

    private final Minecraft mc = Minecraft.getMinecraft();

    @Subscriber
    public void onUpdate(UpdateEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (messageTimer.hasPassed(ExtraChat.delay.getValue() * 1000) && !msgQueue.isEmpty()) {
            ExtraChat.PartyMessage poll = msgQueue.poll();
            if(mc.player.connection.getPlayerInfo(poll.getName()) != null) {
                mc.player.connection.sendPacket(new CPacketChatMessage("/" + (ExtraChat.mode.getValue() == ExtraChat.Mode.MSG ? "msg" : "w") + " " + poll.getName() + " " + poll.getMessage()));
                messageTimer.reset();
            }
        }
    }

    @Subscriber
    public void onBackupRequested(DirectMessageEvent event) {
        msgQueue.add(new ExtraChat.PartyMessage(event.getName(), event.getMessage()));
    }

    @Subscriber
    public void onWorldLoad(WorldEvent.Unload event) {
        msgQueue.clear();
    }

    @Subscriber
    public void onGuiLoad(LoadGuiEvent event) {
        if(event.getGui() instanceof GuiIngameMenu || event.getGui() instanceof GuiDisconnected) {
            Config.save(Config.currentConfig);
        }
    }


}
