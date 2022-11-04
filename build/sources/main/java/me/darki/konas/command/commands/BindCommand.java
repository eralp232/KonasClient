package me.darki.konas.command.commands;

import me.darki.konas.command.Command;
import me.darki.konas.command.chunks.KeyChunk;
import me.darki.konas.command.chunks.ModuleChunk;
import me.darki.konas.module.Module;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.util.client.ChatUtil;
import org.lwjgl.input.Keyboard;

public class BindCommand extends Command {

    public BindCommand() {
        super("bind", "Bind key to module", new String[]{"b"}, new ModuleChunk("<Module>"), new KeyChunk("<Bind>"));
    }

    @Override
    public void onFire(String[] args) {
        if (args.length != getChunks().size() + 1) {
            ChatUtil.error(getChunksAsString());
            return;
        }

        Module module;
        if (ModuleManager.getModuleByName(args[1]) != null) {
            module = ModuleManager.getModuleByName(args[1]);
        } else {
            ChatUtil.error("Module not found");
            return;
        }

        int bind;
        if (Keyboard.getKeyIndex(args[2].toUpperCase()) != 0) {
            bind = Keyboard.getKeyIndex(args[2].toUpperCase());
        } else if (args[2].equalsIgnoreCase("none")) {
            bind = Keyboard.KEY_NONE;
        } else {
            ChatUtil.error("Invalid key");
            return;
        }

        module.setKeybind(bind);
        ChatUtil.info("Bound (h)%s(r) to (h)%s", module.getName(), Keyboard.getKeyName(bind));
    }


}
