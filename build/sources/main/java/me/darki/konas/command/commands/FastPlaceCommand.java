package me.darki.konas.command.commands;

import me.darki.konas.command.Command;
import me.darki.konas.command.SyntaxChunk;
import me.darki.konas.module.modules.player.FastUse;
import me.darki.konas.util.Logger;

public class FastPlaceCommand extends Command {
    public FastPlaceCommand() {
        super("fastplace", "Add and remove blocks from fastplace whitelist", new SyntaxChunk("<add/del/list>"), new SyntaxChunk("[block]"));
    }

    @Override
    public void onFire(String[] args) {
        if (args.length == 2) {
            if (args[1].equalsIgnoreCase("list")) {
                if (!FastUse.whitelist.getValue().getBlocksAsString().isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("\n");
                    FastUse.whitelist.getValue().getBlocksAsString().forEach(str -> sb.append(str + "\n"));
                    Logger.sendChatMessage(sb.toString());
                } else {
                    Logger.sendChatMessage("You dont have any blocks added :(");
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
            FastUse.whitelist.getValue().addBlock(args[2]);
        } else if (args[1].equalsIgnoreCase("del")) {
            FastUse.whitelist.getValue().removeBlock(args[2]);
        } else {
            Logger.sendChatMessage(getChunksAsString());
        }

        FastUse.whitelist.getValue().refreshBlocks();
    }
}