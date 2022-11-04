package me.darki.konas.util.client;

import net.minecraft.entity.Entity;

import java.util.ArrayList;

public class FakePlayerManager {
    private static ArrayList<Integer> fakeEntities = new ArrayList<>();

    public static void addEntity(Entity entity) {
        fakeEntities.add(entity.getEntityId());
    }

    public static void addEntity(int id) {
        fakeEntities.add(id);
    }

    public static void delEntity(Entity entity) {
        fakeEntities.remove((Integer) entity.getEntityId());
    }

    public static void delEntity(int id) {
        fakeEntities.remove((Integer) id);
    }

    public static boolean isFake(Entity entity) {
        return fakeEntities.contains(entity.getEntityId());
    }

    public static boolean isFake(int id) {
        return fakeEntities.contains(id);
    }

    public static void clear() {
        fakeEntities.clear();
    }
}
