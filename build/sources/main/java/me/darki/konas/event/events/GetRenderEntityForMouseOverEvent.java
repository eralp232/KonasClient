package me.darki.konas.event.events;

import net.minecraft.entity.Entity;

public class GetRenderEntityForMouseOverEvent {
    private Entity entity;

    public GetRenderEntityForMouseOverEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }
}
