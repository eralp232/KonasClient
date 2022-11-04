package me.darki.konas.command.chunks;

import me.darki.konas.command.SyntaxChunk;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

public class PlayerChunk extends SyntaxChunk {
    public PlayerChunk(String name) {
        super(name);
    }

    @Override
    public String predict(String currentArg) {
        for (EntityPlayer player : Minecraft.getMinecraft().world.playerEntities) {
            if (player.getName().toLowerCase().startsWith(currentArg.toLowerCase())) {
                return player.getName();
            }
        }
        return currentArg;
    }
}
