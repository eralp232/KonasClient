package me.darki.konas.command.commands;

import me.darki.konas.command.Command;
import me.darki.konas.command.SyntaxChunk;
import me.darki.konas.util.Logger;

public class PrefixCommand extends Command {

    public PrefixCommand() {
        super("prefix",  "Set the command prefix", new SyntaxChunk("<prefix>"));
    }

    @Override
    public void onFire(String[] args) {
        if (args.length != getChunks().size() + 1) {
            Logger.sendChatMessage(getChunksAsString());
            return;
        }


        Command.setPrefix(args[1]);
        Logger.sendChatMessage("Set Prefix to &b" + getPrefix());
    }


}
