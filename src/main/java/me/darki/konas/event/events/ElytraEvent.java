package me.darki.konas.event.events;

import net.minecraft.entity.Entity;

public class ElytraEvent extends CancellableEvent {
    private static ElytraEvent INSTANCE = new ElytraEvent();

    private Entity entity;

    public static ElytraEvent get(Entity entity) {
        INSTANCE.setCancelled(false);
        INSTANCE.entity = entity;
        return INSTANCE;
    }

    public Entity getEntity() {
        return entity;
    }
}
