package me.darki.konas.event.events;

public class DismountRidingEntityEvent extends CancellableEvent {
    private static DismountRidingEntityEvent INSTANCE = new DismountRidingEntityEvent();

    public static DismountRidingEntityEvent get() {
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }
}
