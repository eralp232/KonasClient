package me.darki.konas.event.events;

public class PlayerTravelEvent extends CancellableEvent {
    private final float strafe;
    private final float vertical;
    private final float forward;

    public PlayerTravelEvent(float strafe, float vertical, float forward) {
        this.strafe = strafe;
        this.vertical = vertical;
        this.forward = forward;
    }

    public float getStrafe() {
        return strafe;
    }

    public float getVertical() {
        return vertical;
    }

    public float getForward() {
        return forward;
    }
}
