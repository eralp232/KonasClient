package me.darki.konas.event.events;

import net.minecraft.entity.Entity;

public class RenderEntityEvent extends CancellableEvent {
    private final Entity entity;

    private double x;
    private double y;
    private double z;

    private float yaw;

    private final float partialTicks;

    public RenderEntityEvent(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        this.entity = entity;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.partialTicks = partialTicks;
    }

    public static class Pre extends RenderEntityEvent {
        public Pre(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
            super(entity, x, y, z, yaw, partialTicks);
        }
    }

    public static class Post extends RenderEntityEvent {
        public Post(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
            super(entity, x, y, z, yaw, partialTicks);
        }
    }

    public Entity getEntity() {
        return entity;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPartialTicks() {
        return partialTicks;
    }
}
