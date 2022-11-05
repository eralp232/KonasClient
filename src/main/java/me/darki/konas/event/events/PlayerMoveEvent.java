package me.darki.konas.event.events;

import net.minecraft.entity.MoverType;

public class PlayerMoveEvent extends CancellableEvent {
    private static PlayerMoveEvent INSTANCE = new PlayerMoveEvent();

    private MoverType type;
    private double x;
    private double y;
    private double z;

    public static PlayerMoveEvent get(MoverType type, double x, double y, double z) {
        INSTANCE.setCancelled(false);
        INSTANCE.type = type;
        INSTANCE.x = x;
        INSTANCE.y = y;
        INSTANCE.z = z;
        return INSTANCE;
    }

    public MoverType getType() {
        return type;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public void setType(MoverType type) {
        this.type = type;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }
}
