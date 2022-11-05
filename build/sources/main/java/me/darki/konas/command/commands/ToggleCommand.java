package me.darki.konas.command.commands;

import me.darki.konas.command.Command;
import me.darki.konas.command.chunks.ModuleChunk;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.util.Logger;

public class ToggleCommand extends Command {

    public ToggleCommand() {
        super("toggle", "Toggle Modules", new String[]{"t"}, new ModuleChunk("<module>"));
    }

    @Override
    public void onFire(String[] args) {
        if (args.length != getChunks().size() + 1) {
            Logger.sendChatErrorMessage(getChunksAsString());
            return;
        }

        if (ModuleManager.getModuleByName(args[1]) != null) {
            ModuleManager.getModuleByName(args[1]).toggle();
        } else {
            Logger.sendChatErrorMessage("Invalid Module");
        }

    }


}
