package me.darki.konas.event.events;

import net.minecraft.entity.Entity;

public class TotemPopEvent {

    private final Entity player;
    private final int pops;

    public TotemPopEvent(Entity player, int pops) {
        this.player = player;
        this.pops = pops;
    }

    public Entity getPlayer() {
        return player;
    }

    public int getPops() {
        return pops;
    }
}
