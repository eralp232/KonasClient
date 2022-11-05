package me.darki.konas.event.events;

public class CancellableEvent {

    private boolean cancelled = false;

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public void cancel() {
        this.cancelled = true;
    }

    public boolean isCancelled() {
        return cancelled;
    }
}
