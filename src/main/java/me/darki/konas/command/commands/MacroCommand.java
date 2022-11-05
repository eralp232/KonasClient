package me.darki.konas.command.commands;

import me.darki.konas.command.Command;
import me.darki.konas.command.SyntaxChunk;
import me.darki.konas.command.chunks.KeyChunk;
import me.darki.konas.macro.Macro;
import me.darki.konas.macro.MacroManager;
import me.darki.konas.util.Logger;
import org.lwjgl.input.Keyboard;

import java.util.Arrays;

public class MacroCommand extends Command {

    public MacroCommand() {
        super("macro", "Macro", new SyntaxChunk("<add/remove/list>"), new SyntaxChunk("<name>"), new KeyChunk("[bind]"), new SyntaxChunk("[text]"));
    }

    @Override
    public void onFire(String[] args) {

        if(args.length == 2) {
            if (args[1].equals("list")) {
                Logger.sendChatMessage("&bMacros:");
                Logger.sendChatMessage(" ");
                MacroManager.getMacros().forEach(macro -> Logger.sendChatMessage(macro.getName() + (macro.getBind() != Keyboard.KEY_NONE ? " [&b" + Keyboard.getKeyName(macro.getBind()) + "&f]" : "") + " {&b" + macro.getText() + "&f}"));
            } else {
                Logger.sendChatMessage(getChunksAsString());
            }
        } else if (args.length == 3) {
            if (args[1].equals("remove")) {
                if (MacroManager.getMacroByName(args[2]) != null) {
                    Macro macro = MacroManager.getMacroByName(args[2]);
                    MacroManager.removeMacro(macro);
                    Logger.sendChatMessage("Removed Macro &b" + macro.getName());
                } else {
                    Logger.sendChatErrorMessage("Couldnt find Macro &b" + args[2]);
                }
            } else {
                Logger.sendChatMessage(getChunksAsString());
            }
        } else if(args.length >= 5) {
            if (args[1].equals("add")) {
                String name = args[2];
                String bind = args[3].toUpperCase();
                String text = String.join(" ", Arrays.copyOfRange(args, 4, args.length));
                if(Keyboard.getKeyIndex(bind) == Keyboard.KEY_NONE) {
                    Logger.sendChatErrorMessage("Please specify a valid keybind!");
                    return;
                }
                Macro macro = new Macro(name, text, Keyboard.getKeyIndex(bind));
                MacroManager.addMacro(macro);
                Logger.sendChatMessage("Added Macro &b" + name + " &fwith Bind &b" + Keyboard.getKeyName(macro.getBind()));
            }else {
                Logger.sendChatMessage(getChunksAsString());
            }
        } else {
            Logger.sendChatMessage(getChunksAsString());
        }

    }

}
