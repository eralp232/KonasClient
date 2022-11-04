package me.darki.konas.module.modules.misc;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.command.Command;
import me.darki.konas.event.events.UpdateEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.Logger;
import me.darki.konas.util.client.FakePlayerManager;
import me.darki.konas.util.friends.Friends;
import net.minecraft.entity.player.EntityPlayer;

import java.awt.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class VisualRange extends Module {

    private Setting<Boolean> oneLine = new Setting<>("OneLine", false);
    private Setting<Boolean> leaves = new Setting<>("Leaves", true);
    private Setting<Boolean> ignoreFriends = new Setting<>("IgnoreFriends", true);
    private Setting<Boolean> notify = new Setting<>("Notify", false).withVisibility(this::isNotif);

    private CopyOnWriteArrayList<EntityPlayer> playersInVisualRange = new CopyOnWriteArrayList<>();

    public VisualRange() {
        super("VisualRange", Category.MISC);
    }

    @Subscriber
    public void onUpdate(UpdateEvent event) {

        if (mc.world == null || mc.player == null) return;

        if (playersInVisualRange == null) {
            playersInVisualRange = new CopyOnWriteArrayList<>();
        }

        for (EntityPlayer e : mc.world.playerEntities) {
            if (e == mc.player) {
                continue;
            }

            if (FakePlayerManager.isFake(e)) {
                continue;
            }

            if (ignoreFriends.getValue() && Friends.isFriend(e.getName())) {
                continue;
            }

            if (!playersInVisualRange.contains(e)) {
                playersInVisualRange.add(e);
                if (oneLine.getValue()) {
                    Logger.sendOptionalDeletableMessage(e.getName() + Command.SECTIONSIGN + "a entered" + Command.SECTIONSIGN + "f Visual Range!", 5555);
                } else {
                    Logger.sendChatMessage(e.getName() + Command.SECTIONSIGN + "a entered" + Command.SECTIONSIGN + "f Visual Range!");
                }
                if (notify.getValue()) {
                    doNotification(e.getName() + " has entered Visual Range!", TrayIcon.MessageType.WARNING);
                }
            }
        }

        for (EntityPlayer e : playersInVisualRange) {
            if (!mc.world.playerEntities.contains(e)) {
                playersInVisualRange.remove(e);
                if (leaves.getValue() && (ignoreFriends.getValue() && !Friends.isFriend(e.getName()))) {
                    if (oneLine.getValue()) {
                        Logger.sendOptionalDeletableMessage(e.getName() + Command.SECTIONSIGN + "c left" + Command.SECTIONSIGN + "f Visual Range!", 5555);
                    } else {
                        Logger.sendChatMessage(e.getName() + Command.SECTIONSIGN + "c left" + Command.SECTIONSIGN + "f Visual Range!");
                    }
                }

            }
        }


    }

}
