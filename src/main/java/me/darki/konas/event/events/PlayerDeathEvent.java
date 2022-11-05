package me.darki.konas.event.events;

import net.minecraft.entity.player.EntityPlayer;

public class PlayerDeathEvent {

    private final EntityPlayer player;

    public PlayerDeathEvent(EntityPlayer player) {
        super();
        this.player = player;
    }

    public EntityPlayer getPlayer() {
        return player;
    }
}
