package me.darki.konas.event.events;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class EntityAttackEvent {
    private static EntityAttackEvent INSTANCE = new EntityAttackEvent();

    private EntityPlayer player;
    private Entity target;

    public static EntityAttackEvent get(EntityPlayer player, Entity target) {
        INSTANCE.player = player;
        INSTANCE.target = target;
        return INSTANCE;
    }

    public Entity getTarget()
    {
        return target;
    }

    public EntityPlayer getPlayer() {
        return player;
    }
}
