package me.darki.konas.command.commands;

import me.darki.konas.command.Command;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.util.Logger;
import org.lwjgl.input.Keyboard;

public class ModulesCommand extends Command {


    public ModulesCommand() {
        super("modules", "List all modules including their given keybinds");
    }

    @Override
    public void onFire(String[] args) {
        Logger.sendChatMessage("&bModules:");
        Logger.sendChatMessage(" ");
        ModuleManager.getModules().forEach(module -> {
            Logger.sendChatMessage(module.getName() + (module.getKeybind() != 0 ? " [&b" + Keyboard.getKeyName(module.getKeybind()) + "&f]" : ""));
        });

    }


}
