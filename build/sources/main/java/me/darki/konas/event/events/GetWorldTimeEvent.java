package me.darki.konas.event.events;

public class GetWorldTimeEvent extends CancellableEvent {

    private long worldTime = 6000;

    public long getWorldTime() {
        return worldTime;
    }

    public void setWorldTime(long worldTime) {
        this.worldTime = worldTime;
    }
}
