package me.darki.konas.event.events;

import net.minecraft.entity.player.EntityPlayer;

public class BlockPushOutEvent extends CancellableEvent {
    private static BlockPushOutEvent INSTANCE = new BlockPushOutEvent();

    private EntityPlayer player;

    public static BlockPushOutEvent get(EntityPlayer player) {
        INSTANCE.setCancelled(false);
        INSTANCE.player = player;
        return INSTANCE;
    }

    public EntityPlayer getPlayer() {
        return player;
    }
}
