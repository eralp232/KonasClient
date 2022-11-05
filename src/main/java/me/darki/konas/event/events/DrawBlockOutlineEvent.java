package me.darki.konas.event.events;

public class DrawBlockOutlineEvent extends CancellableEvent {
    private static DrawBlockOutlineEvent INSTANCE = new DrawBlockOutlineEvent();

    public static DrawBlockOutlineEvent get() {
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }
}
