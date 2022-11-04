package me.darki.konas.command.commands;

import me.darki.konas.command.Command;
import me.darki.konas.command.SyntaxChunk;
import me.darki.konas.command.chunks.ModuleChunk;
import me.darki.konas.config.Config;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.util.client.ChatUtil;

import java.io.File;

public class ModuleConfigCommand extends Command {

    public ModuleConfigCommand() {
        super("moduleconfig", "Load or save specific modules", new String[]{"mc"}, new SyntaxChunk("<load/save>"), new ModuleChunk("<module/friends>"), new SyntaxChunk("<config/default>"));
    }

    @Override
    public void onFire(String[] args) {

        if (args.length != getChunks().size() + 1) {
            ChatUtil.error(getChunksAsString());
            return;
        }

        File file = new File(Config.CONFIGS, args[3] + ".json");

        if(args[3].equalsIgnoreCase("default")) {
            file = Config.CONFIG;
        }

        switch (args[1].toLowerCase()) {
            case "save":
                if (!file.exists()) {
                    ChatUtil.error("Config (h)%s(r) doesn't exist!", args[3]);
                    return;
                }
                if (args[2].equalsIgnoreCase("friends")) {
                    Config.saveOnlyFriends(file);
                    ChatUtil.info("Saved Friends to Config (h)%s", args[3]);
                } else {
                    if (ModuleManager.getModuleByName(args[2]) == null) {
                        ChatUtil.error("Module (h)%s(r) is invalid!", args[2]);
                    }
                    Config.saveSingleModule(ModuleManager.getModuleByName(args[2]), file);
                    ChatUtil.info("Saved Module (h)%s(r) to Config (h)%s", args[2],  args[3]);
                }
                break;
            case "load":
                if (!file.exists()) {
                    ChatUtil.error("Config (h)%s(r) doesn't exist!", args[3]);
                    return;
                }
                if (args[2].equalsIgnoreCase("friends")) {
                    Config.loadOnlyFriends(file);
                    ChatUtil.info("Loaded Friends from Config (h)%s", args[3]);
                } else {
                    if (ModuleManager.getModuleByName(args[2]) == null) {
                        ChatUtil.error("Module (h)%s(r) is invalid", args[2]);
                    }
                    Config.loadSingleModule(ModuleManager.getModuleByName(args[2]), file);
                    ChatUtil.info("Loaded Module (h)%s(r) from Config (h)%s", args[2], args[3]);
                }
                break;
            default:
                ChatUtil.error(getChunksAsString());
        }

    }

}
