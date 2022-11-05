package me.darki.konas.event.events;

import net.minecraft.entity.EntityLivingBase;

public class WalkingTravelEvent {
    private final int id;
    private float strafe;
    private float vertical;
    private float forward;

    public WalkingTravelEvent(int id, float strafe, float vertical, float forward) {
        this.id = id;
        this.strafe = strafe;
        this.vertical = vertical;
        this.forward = forward;
    }

    public int getId() {
        return id;
    }

    public float getStrafe() {
        return strafe;
    }

    public void setStrafe(float strafe) {
        this.strafe = strafe;
    }

    public float getVertical() {
        return vertical;
    }

    public void setVertical(float vertical) {
        this.vertical = vertical;
    }

    public float getForward() {
        return forward;
    }

    public void setForward(float forward) {
        this.forward = forward;
    }
}
