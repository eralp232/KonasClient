package me.darki.konas.command.commands;

import me.darki.konas.command.Command;
import me.darki.konas.command.SyntaxChunk;
import me.darki.konas.util.Logger;
import net.minecraft.entity.Entity;

public class VanishCommand extends Command {

    private Entity ridingEntity;

    public VanishCommand() {
        super("entitydesync", "Entity Desync", new String[]{"vanish"}, new SyntaxChunk("<Dismount/Remount>"));
    }

    @Override
    public void onFire(String[] args) {
        if (args.length == 2) {
            if (args[1].equalsIgnoreCase("Dismount") || args[1].equalsIgnoreCase("D") || args[1].equalsIgnoreCase("Dis")) {
                // dismount
                if (mc.player.getRidingEntity() != null) {
                    this.ridingEntity = mc.player.getRidingEntity();
                    mc.player.dismountRidingEntity();
                    mc.world.removeEntity(ridingEntity);
                    Logger.sendChatMessage("Dismounted entity.");
                } else {
                    Logger.sendChatMessage("You are not riding an entity.");
                }
            } else if (args[1].equalsIgnoreCase("Remount") || args[1].equalsIgnoreCase("R") || args[1].equalsIgnoreCase("Re")) {
                // remount
                if (ridingEntity != null) {
                    ridingEntity.isDead = false;
                    mc.world.addEntityToWorld(ridingEntity.getEntityId(), ridingEntity);
                    mc.player.startRiding(ridingEntity, true);
                    this.ridingEntity = null;
                } else {
                    Logger.sendChatMessage("No entity to remount.");
                }
            } else {
                Logger.sendChatErrorMessage("Please enter either \"Dismount\" or \"Remount\"");
            }
        } else {
            Logger.sendChatMessage(getChunksAsString());
        }

    }
}
