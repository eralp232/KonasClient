package me.darki.konas.event.events;

public class StopUsingItemEvent extends CancellableEvent {
    private boolean packet = false;

    public boolean isPacket() {
        return packet;
    }

    public void setPacket(boolean packet) {
        this.packet = packet;
    }
}
