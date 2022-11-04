package me.darki.konas.event.events;

public class InteractEvent {

    private boolean interacting;

    public InteractEvent(boolean interacting) {
        this.interacting = interacting;
    }

    public boolean isInteracting() {
        return interacting;
    }

    public void setInteracting(boolean interacting) {
        this.interacting = interacting;
    }
}
