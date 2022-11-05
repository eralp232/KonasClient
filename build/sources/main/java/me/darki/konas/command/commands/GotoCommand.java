package me.darki.konas.command.commands;

import me.darki.konas.command.Command;
import me.darki.konas.command.SyntaxChunk;
import me.darki.konas.module.Module;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.module.modules.movement.AutoWalk;
import me.darki.konas.util.Logger;
import me.darki.konas.util.pathfinding.generation.WalkingPathGenerator;
import net.minecraft.util.math.BlockPos;

public class GotoCommand extends Command {
    public GotoCommand() {
        super("goto", "Go to coordinates", new String[]{"go"}, new SyntaxChunk("<X>"), new SyntaxChunk("<Y>"), new SyntaxChunk("<Z>"));
    }

    @Override
    public void onFire(String[] args) {
        if (args.length != getChunks().size() + 1) {
            Logger.sendChatMessage(getChunksAsString());
            return;
        }

        Module module = ModuleManager.getModuleByName("AutoWalk");

        if (module == null) return;

        if (!(module instanceof AutoWalk)) return;

        AutoWalk autoWalk = (AutoWalk) module;

        autoWalk.index = 0;
        autoWalk.done = false;
        try {
            autoWalk.walkingPathGenerator = new WalkingPathGenerator(new BlockPos(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3])), "GOTO-WALK");
        } catch (Exception e) {
            Logger.sendChatMessage(getChunksAsString());
            return;
        }
        autoWalk.walkingPathGenerator.cycle();
        autoWalk.pathFind.setValue(true);
        if (!autoWalk.isEnabled()) {
            autoWalk.toggle();
        }
    }
}