package me.darki.konas.command.commands;

import me.darki.konas.command.Command;
import me.darki.konas.command.SyntaxChunk;
import me.darki.konas.module.modules.player.InvCleaner;
import me.darki.konas.util.Logger;

public class InvCleanerCommand extends Command {
    public InvCleanerCommand() {
        super("invcleaner", "Add and remove items from invcleaner", new SyntaxChunk("<add/del/list>"), new SyntaxChunk("[item]"));
    }

    @Override
    public void onFire(String[] args) {
        if (args.length == 2) {
            if (args[1].equalsIgnoreCase("list")) {
                if (!InvCleaner.items.getValue().getItems().isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("\n");
                    InvCleaner.items.getValue().getItems().forEach(str -> sb.append(str + "\n"));
                    Logger.sendChatMessage(sb.toString());
                } else {
                    Logger.sendChatMessage("You dont have any items added :(");
                }
            } else {
                Logger.sendChatMessage(getChunksAsString());
            }
            return;
        }
        if (args.length != getChunks().size() + 1) {
            Logger.sendChatMessage(getChunksAsString());
            return;
        }

        if (args[1].equalsIgnoreCase("add")) {
            InvCleaner.items.getValue().addItem(args[2]);
        } else if (args[1].equalsIgnoreCase("del")) {
            InvCleaner.items.getValue().removeItem(args[2]);
        } else {
            Logger.sendChatMessage(getChunksAsString());
        }
    }
}

