package me.darki.konas.command.commands;

import com.google.common.io.Files;
import me.darki.konas.command.Command;
import me.darki.konas.command.SyntaxChunk;
import me.darki.konas.config.Config;
import me.darki.konas.util.Logger;
import me.darki.konas.util.client.ChatUtil;

import java.io.File;
import java.util.List;

public class ConfigCommand extends Command {

    public ConfigCommand() {
        super("config", "Load different configs", new SyntaxChunk("<create/save/load/forceload/delete/list/current>"), new SyntaxChunk("<name/default>"));
    }

    @Override
    public void onFire(String[] args) {
        if (args.length != getChunks().size() && args.length != getChunks().size() + 1) {
            Logger.sendChatErrorMessage(getChunksAsString());
            return;
        }

        if (!Config.CONFIGS.exists()) Config.CONFIGS.mkdir();

        if (args[1].equalsIgnoreCase("list")) {
            List<File> list = Config.getConfigList();

            ChatUtil.info("(h)Configs:");
            ChatUtil.lineBreak();

            if (Config.currentConfig.toPath() == Config.CONFIG.toPath()) ChatUtil.info("(h)default");
            else ChatUtil.info("default");

            if (list != null && !list.isEmpty()) {
                for (File file : list) {
                    if (file.toPath() == Config.currentConfig.toPath()) ChatUtil.info("(h)%s", Files.getNameWithoutExtension(file.getName()));
                    else ChatUtil.info("%s", Files.getNameWithoutExtension(file.getName()));
                }
            }
            return;
        } else if (args[1].equalsIgnoreCase("current")) {
            if (Config.currentConfig == Config.CONFIG) ChatUtil.info("Currently selected config: (h)default");
            else ChatUtil.info("Currently selected config: (h)%s", Files.getNameWithoutExtension(Config.currentConfig.getName()));
            return;
        } else if(args.length == 2) {
            ChatUtil.error(getChunksAsString());
            return;
        }

        File file = new File(Config.CONFIGS, args[2] + ".json");

        boolean isDefault = args[2].equalsIgnoreCase("default");

        if (isDefault) file = Config.CONFIG;

        switch (args[1].toLowerCase()) {
            case "create":
                if (file.exists()) {
                    ChatUtil.error("Config (h)%s(r) already exists!",  isDefault ? "default" : args[2]);
                    return;
                }
                Config.save(Config.currentConfig);
                Config.save(file);
                Config.load(file, false);
                ChatUtil.info("Created Config (h)%s", isDefault ? "default" : args[2]);
                break;
            case "save":
                if (!file.exists()) {
                    ChatUtil.error("Config (h)%s(r) doesn't exist!",  isDefault ? "default" : args[2]);
                    return;
                }
                Config.save(file);
                ChatUtil.info("Saved Config (h)%s", isDefault ? "default" : args[2]);
                break;
            case "load":
                if (!file.exists()) {
                    ChatUtil.error("Config (h)%s(r) doesn't exist!",  isDefault ? "default" : args[2]);
                    return;
                }
                Config.save(Config.currentConfig);
                Config.load(file, false);
                ChatUtil.info("Loaded Config (h)%s", isDefault ? "default" : args[2]);
                break;
            case "forceload":
                if (!file.exists()) {
                    ChatUtil.error("Config (h)%s(r) doesn't exist!",  isDefault ? "default" : args[2]);
                    return;
                }
                Config.load(file, false);
                ChatUtil.info("Forceloaded Config (h)%s",  isDefault ? "default" : args[2]);
                break;
            case "delete":
                if (isDefault) {
                    ChatUtil.error("You can't delete the (h)Default(r) Config");
                    return;
                }
                if (!file.exists()) {
                    ChatUtil.error("Config (h)%s(r) doesn't exist!", args[2]);
                    return;
                }
                if (Config.delete(file)) {
                    ChatUtil.info("Deleted Config (h)%s", args[2]);
                } else {
                    ChatUtil.error("Couldn't delete Config (h)%s", args[2]);
                }
                break;
            default:
                ChatUtil.error(getChunksAsString());
        }

    }

}