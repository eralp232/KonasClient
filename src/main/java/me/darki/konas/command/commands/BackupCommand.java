package me.darki.konas.command.commands;

import cookiedragon.eventsystem.EventDispatcher;
import me.darki.konas.command.Command;
import me.darki.konas.event.events.DirectMessageEvent;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.module.modules.misc.ExtraChat;

public class BackupCommand extends Command {

    public BackupCommand() {
        super("backup", "Notifies your party that you need backup");
    }

    @Override
    public void onFire(String[] args) {
        String backupString ="I need backup at X:" + mc.player.getPosition().getX() + " Y:" + mc.player.getPosition().getY() + " Z:" + mc.player.getPosition().getZ() + " in the " + (mc.player.dimension == -1 ? "Nether" : "Overworld");
        if(ModuleManager.getModuleByClass(ExtraChat.class).isEnabled() && ExtraChat.global.getValue()) {
            mc.player.sendChatMessage(backupString);
        } else {
            for(String name : PartyCommand.party) {
                EventDispatcher.Companion.dispatch(new DirectMessageEvent(name, backupString));
            }
        }

    }

}
