package me.darki.konas.command.commands;

import me.darki.konas.command.Command;
import me.darki.konas.command.SyntaxChunk;
import me.darki.konas.module.modules.render.XRay;
import me.darki.konas.util.Logger;

public class XRayCommand extends Command {
    public XRayCommand() {
        super("xray", "Add and remove blocks from xray", new SyntaxChunk("<add/del/list>"), new SyntaxChunk("[block]"));
    }

    @Override
    public void onFire(String[] args) {
        if (args.length == 2) {
            if (args[1].equalsIgnoreCase("list")) {
                if (!XRay.blocks.getValue().getBlocksAsString().isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("\n");
                    XRay.blocks.getValue().getBlocksAsString().forEach(str -> sb.append(str + "\n"));
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
            if(XRay.blocks.getValue().addBlock(args[2])) {
                Logger.sendChatMessage("Added Block &b" + args[2]);
                mc.renderGlobal.loadRenderers();
            } else {
                Logger.sendChatErrorMessage("Couldn't find block &b" + args[2]);
            }
        } else if (args[1].equalsIgnoreCase("del")) {
            if(XRay.blocks.getValue().removeBlock(args[2])) {
                Logger.sendChatMessage("Removed Block &b" + args[2]);
                mc.renderGlobal.loadRenderers();
            } else {
                Logger.sendChatErrorMessage("Couldn't find block &b" + args[2]);
            }
        } else {
            Logger.sendChatMessage(getChunksAsString());
        }

        XRay.blocks.getValue().refreshBlocks();
    }
}
