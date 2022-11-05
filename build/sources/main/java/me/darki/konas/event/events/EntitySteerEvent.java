package me.darki.konas.event.events;

import net.minecraft.entity.passive.AbstractHorse;

public class EntitySteerEvent extends CancellableEvent {
    private static EntitySteerEvent INSTANCE = new EntitySteerEvent();

    private AbstractHorse abstractHorse;

    public static EntitySteerEvent get(AbstractHorse abstractHorse) {
        INSTANCE.setCancelled(false);
        INSTANCE.abstractHorse = abstractHorse;
        return INSTANCE;
    }

    public AbstractHorse getAbstractHorse() {
        return abstractHorse;
    }
}
