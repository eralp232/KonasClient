package me.darki.konas.event.events;

public class BossBarEvent extends CancellableEvent {
    private static BossBarEvent INSTANCE = new BossBarEvent();

    public static BossBarEvent get() {
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }
}
