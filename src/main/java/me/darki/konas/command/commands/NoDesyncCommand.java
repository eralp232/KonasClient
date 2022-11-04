package me.darki.konas.command.commands;

import me.darki.konas.command.Command;
import me.darki.konas.command.SyntaxChunk;
import me.darki.konas.module.modules.misc.NoDesync;
import me.darki.konas.util.Logger;

public class NoDesyncCommand extends Command {
    public NoDesyncCommand() {
        super("nodesync", "Add and remove items from nodesync use whitelist", new SyntaxChunk("<add/del/list>"), new SyntaxChunk("[item]"));
    }

    @Override
    public void onFire(String[] args) {
        if (args.length == 2) {
            if (args[1].equalsIgnoreCase("list")) {
                if (!NoDesync.items.getValue().getItems().isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("\n");
                    NoDesync.items.getValue().getItems().forEach(str -> sb.append(str + "\n"));
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
            NoDesync.items.getValue().addItem(args[2]);
        } else if (args[1].equalsIgnoreCase("del")) {
            NoDesync.items.getValue().removeItem(args[2]);
        } else {
            Logger.sendChatMessage(getChunksAsString());
        }
    }
}

