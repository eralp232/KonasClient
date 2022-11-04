package me.darki.konas.event.listener;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.command.Command;
import me.darki.konas.command.CommandManager;
import me.darki.konas.event.events.ChatEvent;
import me.darki.konas.util.Logger;

public class CommandListener {

    public static CommandListener INSTANCE = new CommandListener();

    @Subscriber
    public void onCommand(ChatEvent event) {
        String s = event.getMessage();
        if (s.startsWith(Command.getPrefix())) {
            event.setCancelled(true);

            String[] args = s.replaceAll("([\\s])\\1+", "$1").split(" ");

            if (CommandManager.getCommandByName(args[0]) != null) {
                CommandManager.getCommandByName(args[0]).onFire(args);
            } else {
                Logger.sendChatErrorMessage("Command not found! To view a list of all commands type " + Command.getPrefix() + "commands");
            }
        }
    }

}
