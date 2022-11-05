package me.darki.konas.command.commands;

import me.darki.konas.command.Command;
import me.darki.konas.command.SyntaxChunk;
import me.darki.konas.module.modules.misc.Spammer;
import me.darki.konas.util.Logger;

public class SpammerCommand extends Command {

    public SpammerCommand() {
        super("spammer", "Configure your spammer config file", new SyntaxChunk("load"), new SyntaxChunk("<name>"));
    }

    @Override
    public void onFire(String[] args) {
        if (args.length != getChunks().size() + 1) {
            Logger.sendChatErrorMessage(getChunksAsString());
            return;
        }

        if(args[1].equalsIgnoreCase("load")) {
            String file = args[2].replaceAll(".txt", "") + ".txt";
            Spammer.spammerFile.setValue(file);
            Logger.sendChatMessage("Loaded Spammer File: §b" + file);
        } else {
            Logger.sendChatErrorMessage(getChunksAsString());
            return;
        }

    }



}
