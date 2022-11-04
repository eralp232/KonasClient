package me.darki.konas.command.chunks;

import me.darki.konas.command.SyntaxChunk;
import me.darki.konas.module.Module;
import me.darki.konas.module.ModuleManager;

public class ModuleChunk extends SyntaxChunk {
    public ModuleChunk(String name) {
        super(name);
    }

    @Override
    public String predict(String currentArg) {
        for (Module module : ModuleManager.getModules()) {
            if (module.getName().toLowerCase().startsWith(currentArg.toLowerCase())) {
                return module.getName();
            }
            for (String alias : module.getAliases()) {
                if (alias.toLowerCase().startsWith(currentArg.toLowerCase())) {
                    return alias;
                }
            }
        }
        return currentArg;
    }
}
