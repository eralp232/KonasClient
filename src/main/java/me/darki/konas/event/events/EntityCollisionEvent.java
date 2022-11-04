package me.darki.konas.event.events;

import net.minecraft.entity.Entity;

public class EntityCollisionEvent extends CancellableEvent {
    private static EntityCollisionEvent INSTANCE = new EntityCollisionEvent();

    private Entity entity;
    private double coordinate;
    private Type type;

    public static EntityCollisionEvent get(Entity entity, double coordinate, Type type) {
        INSTANCE.setCancelled(false);
        INSTANCE.entity = entity;
        INSTANCE.coordinate = coordinate;
        INSTANCE.type = type;
        return INSTANCE;
    }

    public Entity getEntity() {
        return entity;
    }

    public double getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(double coordinate) {
        this.coordinate = coordinate;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        HORIZONTAL, VERTICAL
    }

}
