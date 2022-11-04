package me.darki.konas.event.events;

public class BlockResetEvent extends CancellableEvent {
    private static BlockResetEvent INSTANCE = new BlockResetEvent();

    public static BlockResetEvent get() {
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }
}
