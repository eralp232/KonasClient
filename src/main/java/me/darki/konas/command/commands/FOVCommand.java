package me.darki.konas.command.commands;

import me.darki.konas.command.Command;
import me.darki.konas.command.SyntaxChunk;
import me.darki.konas.util.Logger;

public class FOVCommand extends Command {

    public FOVCommand() {
        super("FOV", "Sets your FOV to the value entered.", new String[]{"SetFOV", "FieldOfView"}, new SyntaxChunk("<FOV>"));
    }

    @Override
    public void onFire(String[] args) {
        if (args.length == 2) {
            try {
                if (args[1].equalsIgnoreCase("ergeisacoolgamer")) {
                    Logger.sendChatMessage("epicjbug is cooler!");
                } else {
                    mc.gameSettings.fovSetting = Float.parseFloat(args[1]);
                    Logger.sendChatMessage("Set FOV to " + Float.parseFloat(args[1]));
                }
            } catch (NumberFormatException exception) {
                Logger.sendChatErrorMessage("Please enter a valid FOV!");
            }
        } else {
            Logger.sendChatMessage(getChunksAsString());
        }

    }
}
