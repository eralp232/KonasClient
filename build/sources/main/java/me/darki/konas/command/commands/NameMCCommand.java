package me.darki.konas.command.commands;

import me.darki.konas.command.Command;
import me.darki.konas.command.chunks.PlayerChunk;
import me.darki.konas.util.Logger;
import me.darki.konas.util.client.ChatUtil;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class NameMCCommand extends Command {

    public NameMCCommand() {
        super("namemc", "Opens up the NameMC Page for the specified player", new String[]{"nmc"}, new PlayerChunk("[player]"));
    }

    @Override
    public void onFire(String[] args) {
        if(args.length != 2) {
            ChatUtil.error(getChunksAsString());
            return;
        }

        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI("https://namemc.com/search?q=" + args[1]));
            } catch (URISyntaxException | IOException ignored) {}
        }

    }


}
