package me.darki.konas.command.commands;

import me.darki.konas.command.Command;
import me.darki.konas.command.SyntaxChunk;
import me.darki.konas.module.modules.render.Search;
import me.darki.konas.util.Logger;

public class SearchCommand extends Command {
    public SearchCommand() {
        super("search", "Add and remove blocks from search", new SyntaxChunk("<add/del/list>"), new SyntaxChunk("[block]"));
    }

    @Override
    public void onFire(String[] args) {
        if (args.length == 2) {
            if (args[1].equalsIgnoreCase("list")) {
                if (!Search.customBlocks.getValue().getBlocksAsString().isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("\n");
                    Search.customBlocks.getValue().getBlocksAsString().forEach(str -> sb.append(str + "\n"));
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
            if(Search.customBlocks.getValue().addBlock(args[2])) {
                Logger.sendChatMessage("Added Block &b" + args[2]);
                if (Search.softReload.getValue()) {
                    Search.doSoftReload();
                }
            } else {
                Logger.sendChatErrorMessage("Couldn't find block &b" + args[2]);
            }
        } else if (args[1].equalsIgnoreCase("del")) {
            if(Search.customBlocks.getValue().removeBlock(args[2])) {
                Logger.sendChatMessage("Removed Block &b" + args[2]);
            } else {
                Logger.sendChatErrorMessage("Couldn't find block &b" + args[2]);
            }
        } else {
            Logger.sendChatMessage(getChunksAsString());
        }

        Search.customBlocks.getValue().refreshBlocks();
    }
}

