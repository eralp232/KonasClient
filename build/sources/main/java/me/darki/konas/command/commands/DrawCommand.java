package me.darki.konas.command.commands;

import me.darki.konas.command.Command;
import me.darki.konas.command.chunks.ModuleChunk;
import me.darki.konas.module.Module;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.util.Logger;

public class DrawCommand extends Command {

    public DrawCommand() {
        super("draw", "Makes Modules Visible or Invisible on the ArrayList", new ModuleChunk("<Module>"));
    }

    @Override
    public void onFire(String[] args) {
        if (args.length != getChunks().size() + 1) {
            Logger.sendChatErrorMessage(getChunksAsString());
            return;
        }

        Module m = ModuleManager.getModuleByName(args[1]);

        if (m != null) {
            m.setVisible(!m.isVisible());
            Logger.sendChatMessage("Drawn Module &b" + m.getName());
        } else {
            Logger.sendChatErrorMessage("Unknown Module &b" + args[1]);
        }

    }

}
