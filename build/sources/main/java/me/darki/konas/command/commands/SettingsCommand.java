package me.darki.konas.command.commands;

import me.darki.konas.command.Command;
import me.darki.konas.command.chunks.ModuleChunk;
import me.darki.konas.module.Module;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.setting.Bind;
import me.darki.konas.setting.ColorSetting;
import me.darki.konas.setting.Setting;
import me.darki.konas.setting.SubBind;
import me.darki.konas.util.Logger;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class SettingsCommand extends Command {

    public SettingsCommand() {
        super("settings", "Shows you a modules settings", new ModuleChunk("<module>"));
    }

    @Override
    public void onFire(String[] args) {
        if (args.length != getChunks().size() + 1) {
            Logger.sendChatMessage(getChunksAsString());
            return;
        }

        Module module;
        if (ModuleManager.getModuleByName(args[1]) != null) {
            module = ModuleManager.getModuleByName(args[1]);
        } else {
            Logger.sendChatMessage("Module not found");
            return;
        }

        if (ModuleManager.getSettingList(module) == null) {
            Logger.sendChatErrorMessage(module.getName() + " does not have any settings");
            return;
        }

        Logger.sendChatMessage(module.getName() + "'s Settings:");
        Logger.sendChatMessage(" ");

        for (Setting setting : ModuleManager.getSettingList(module)) {
            if (setting.getValue() instanceof Enum) {
                List<String> options = new ArrayList<>();
                for (Object e : setting.getValue().getClass().getEnumConstants()) {
                    options.add(e.toString());
                }
                Logger.sendChatMessage(setting.getName() + " [&b" + setting.getValue() + "&f] &b" + options + "&f");
            } else if(setting.getValue() instanceof Bind) {
                Logger.sendChatMessage(setting.getName() + " [&b" + Keyboard.getKeyName(((Bind) setting.getValue()).getKeyCode()) + "&f]");
            } else if(setting.getValue() instanceof SubBind) {
                Logger.sendChatMessage(setting.getName() + " [&b" + Keyboard.getKeyName(((SubBind) setting.getValue()).getKeyCode()) + "&f]");
            } else if(setting.getValue() instanceof ColorSetting) {
                Logger.sendChatMessage(setting.getName() + " [&b" + ((ColorSetting) setting.getValue()).getRawColor() + ", " + ((ColorSetting) setting.getValue()).isCycle() + "&f]");
            } else {
                Logger.sendChatMessage(setting.getName() + " [&b" + setting.getValue() + "&f]" + (setting.getMin() != null && setting.getMax() != null ? " Min:&b " + setting.getMin() + "&f Max:&b " + setting.getMax() : ""));
            }
        }


    }


}
