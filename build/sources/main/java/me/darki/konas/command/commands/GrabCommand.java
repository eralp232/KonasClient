package me.darki.konas.command.commands;

import me.darki.konas.command.Command;
import me.darki.konas.util.Logger;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;


public class GrabCommand extends Command {

    public GrabCommand() {
        super("Grab", "Copies your current coords to your clipboard", new String[]{"Coords", "CopyCoords", "CopyPos", "CopyPosition"});
    }

    @Override
    public void onFire(String[] args) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable transferable = new StringSelection(Math.round(mc.player.posX) + ", " + Math.round(mc.player.posY) + ", " + Math.round(mc.player.posZ));
        clipboard.setContents(transferable, null);
        Logger.sendChatMessage("Copied coords to clipboard!");
    }
}
