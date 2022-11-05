package me.darki.konas.command.commands;

import me.darki.konas.command.Command;
import me.darki.konas.command.SyntaxChunk;
import me.darki.konas.util.Logger;

public class VClipCommand extends Command {

    public VClipCommand() {
        super("VClip", "Teleport you vertically", new String[]{"PosV"}, new SyntaxChunk("<Distance>"));
    }

    @Override
    public void onFire(String[] args) {
        if (args.length == 2) {
            try {
                if (mc.player.getRidingEntity() != null) {
                    mc.player.getRidingEntity().setPosition(mc.player.getRidingEntity().posX, mc.player.getRidingEntity().posY + Double.parseDouble(args[1]), mc.player.getRidingEntity().posZ);
                } else {
                    mc.player.setPosition(mc.player.posX, mc.player.posY + Double.parseDouble(args[1]), mc.player.posZ);
                }
                Logger.sendChatMessage("Teleported you " + (Double.parseDouble(args[1]) > 0 ? "up " : "down ") + Math.abs(Double.parseDouble(args[1])) + " blocks.");
            } catch (NumberFormatException exception) {
                Logger.sendChatErrorMessage("Please enter a valid distance!");
            }
        } else {
            Logger.sendChatMessage(getChunksAsString());
        }

    }
}
