package me.darki.konas.command.commands;

import me.darki.konas.command.Command;
import me.darki.konas.command.CommandManager;
import me.darki.konas.util.client.ChatUtil;

public class CommandsCommand extends Command {


    public CommandsCommand() {
        super("commands", "List all commands");
    }

    @Override
    public void onFire(String[] args) {
        ChatUtil.info("(b)Commands:");
        ChatUtil.lineBreak();
        CommandManager.getCommands().forEach(command -> {
            ChatUtil.info("%s (h)%s(r)" + (command.getDescription() != null ? "() -> %s" : ""), command.getName(), command.getChunksAsString(), command.getDescription());
        });
    }


}
