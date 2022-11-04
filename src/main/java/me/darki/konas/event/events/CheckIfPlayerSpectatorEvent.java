package me.darki.konas.event.events;

public class CheckIfPlayerSpectatorEvent extends CancellableEvent {
    private static CheckIfPlayerSpectatorEvent INSTANCE = new CheckIfPlayerSpectatorEvent();

    public static CheckIfPlayerSpectatorEvent get() {
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }
}
