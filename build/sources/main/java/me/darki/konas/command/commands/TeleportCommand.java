package me.darki.konas.command.commands;

import me.darki.konas.command.Command;
import me.darki.konas.command.SyntaxChunk;
import me.darki.konas.util.Logger;

public class TeleportCommand extends Command {

    public TeleportCommand() {
        super("Teleport", "Teleports you to an X, Y, Z coordinate.", new String[]{"PosH"}, new SyntaxChunk("<X Pos>"), new SyntaxChunk("<Y Pos>"), new SyntaxChunk("<Z Pos>"));
    }

    @Override
    public void onFire(String[] args) {
        try {
            if (args.length == 4) {

                final double x = Double.parseDouble(args[1]);
                final double y = Double.parseDouble(args[2]);
                final double z = Double.parseDouble(args[3]);


                if (mc.player.getRidingEntity() != null) {
                    mc.player.getRidingEntity().setPosition( x, y, z);
                } else {
                    mc.player.setPosition(x, y, z);
                }

                Logger.sendChatMessage("Teleported you to (" + x + ", " + y + ", " + z + ")");
            } else if (args.length == 3) {

                final double x = Double.parseDouble(args[1]);
                final double z = Double.parseDouble(args[2]);

                if (mc.player.getRidingEntity() != null) {
                    mc.player.getRidingEntity().setPosition(x, mc.player.getRidingEntity().posY, z);
                } else {
                    mc.player.setPosition(x, mc.player.posY, z);
                }

                Logger.sendChatMessage("Teleported you to (" + x + ", " + z + ")");

            } else {
                Logger.sendChatMessage(getChunksAsString());
            }
        } catch (NumberFormatException exception) {
            Logger.sendChatErrorMessage("Please enter valid positions!");
        }


    }
}
