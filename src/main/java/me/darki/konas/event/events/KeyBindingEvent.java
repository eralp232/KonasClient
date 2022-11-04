package me.darki.konas.event.events;

public class KeyBindingEvent extends CancellableEvent {

    public boolean holding;
    public boolean pressed;

    public KeyBindingEvent(boolean holding, boolean pressed) {
        super();
        this.holding = holding;
        this.pressed = pressed;
    }

    public boolean isHolding() {
        return holding;
    }

    public boolean isPressed() {
        return pressed;
    }
}
