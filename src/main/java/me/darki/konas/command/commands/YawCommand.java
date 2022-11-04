package me.darki.konas.command.commands;

import me.darki.konas.command.Command;
import me.darki.konas.command.SyntaxChunk;
import me.darki.konas.util.Logger;
import net.minecraft.util.math.MathHelper;

public class YawCommand extends Command {
    public YawCommand() {
        super("yaw", "Set Player's Yaw", new String[]{"setyaw"}, new SyntaxChunk("<x/yaw>"), new SyntaxChunk("[z]"));
    }

    @Override
    public void onFire(String[] args) {
        if (args.length == 2) {
            try {
                if (mc.player != null) {
                    mc.player.rotationYaw = (float) MathHelper.wrapDegrees(Double.parseDouble(args[1]));
                }
            } catch (NumberFormatException exception) {
                Logger.sendChatErrorMessage("Please enter a valid yaw!");
            }
        } else if (args.length == 3) {
            try {
                if (mc.player != null) {
                    mc.player.rotationYaw = (float) MathHelper.wrapDegrees((Math.toDegrees(Math.atan2(mc.player.posZ - Double.parseDouble(args[2]), mc.player.posX - Double.parseDouble(args[1]))) + 90.0));
                }
            } catch (NumberFormatException exception) {
                Logger.sendChatErrorMessage("Please enter a valid yaw!");
            }
        }
    }
}
