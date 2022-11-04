package me.darki.konas.event.events;

import net.minecraft.util.EnumHandSide;

public class ItemRotateEvent extends CancellableEvent {

    private EnumHandSide hand;

    public ItemRotateEvent(EnumHandSide hand) {
        this.hand = hand;
    }

    public EnumHandSide getHand() {
        return hand;
    }
}
