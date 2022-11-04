package me.darki.konas.event.events;

import net.minecraft.entity.player.EntityPlayer;

public class TargetKillEvent {

    private final EntityPlayer target;

    public TargetKillEvent(EntityPlayer target) {
        this.target = target;
    }

    public EntityPlayer getTarget() {
        return target;
    }
}
