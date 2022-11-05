package me.darki.konas.event.events;

import net.minecraft.client.entity.AbstractClientPlayer;

public class RenderPlayerEvent extends CancellableEvent {

    private AbstractClientPlayer entity;

    public RenderPlayerEvent(AbstractClientPlayer entity) {
        this.entity = entity;
    }

    public AbstractClientPlayer getEntity() {
        return entity;
    }
}
