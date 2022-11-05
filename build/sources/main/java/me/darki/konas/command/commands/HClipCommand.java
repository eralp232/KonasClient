package me.darki.konas.command.commands;

import me.darki.konas.command.Command;
import me.darki.konas.command.SyntaxChunk;
import me.darki.konas.util.Logger;
import me.darki.konas.util.client.PlayerUtils;

public class HClipCommand extends Command {

    public HClipCommand() {
        super("HClip", "Teleport you horizontally", new String[]{"PosH"}, new SyntaxChunk("<Distance>"));
    }

    @Override
    public void onFire(String[] args) {
        if (args.length == 2) {
            try {
                final double[] direction = PlayerUtils.getDirectionFromYaw(mc.player.rotationYaw);
                if (mc.player.getRidingEntity() != null) {
                    mc.player.getRidingEntity().setPosition(mc.player.getRidingEntity().posX + direction[0] * Double.parseDouble(args[1]), mc.player.getRidingEntity().posY, mc.player.getRidingEntity().posZ + direction[1] * Double.parseDouble(args[1]));
                } else {
                    mc.player.setPosition(mc.player.posX + direction[0] * Double.parseDouble(args[1]), mc.player.posY, mc.player.posZ + direction[1] * Double.parseDouble(args[1]));
                }
                Logger.sendChatMessage("Teleported you " + (Double.parseDouble(args[1]) > 0 ? "forwards " : "backwards ") + Math.abs(Double.parseDouble(args[1])) + " blocks.");
            } catch (NumberFormatException exception) {
                Logger.sendChatErrorMessage("Please enter a valid distance!");
            }
        } else {
            Logger.sendChatMessage(getChunksAsString());
        }

    }
}
