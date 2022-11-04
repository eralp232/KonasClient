package me.darki.konas.event.events;

import net.minecraft.entity.passive.AbstractHorse;

public class HorseSaddledEvent extends CancellableEvent {

    private AbstractHorse abstractHorse;

    public HorseSaddledEvent(AbstractHorse abstractHorse) {
        this.abstractHorse = abstractHorse;
    }

    public AbstractHorse getAbstractHorse() {
        return abstractHorse;
    }

    public void setAbstractHorse(AbstractHorse abstractHorse) {
        this.abstractHorse = abstractHorse;
    }

}
