package me.darki.konas.event.events;

import net.minecraft.entity.Entity;

public class FreecamEntityEvent {

    private Entity entity;

    public FreecamEntityEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }
}