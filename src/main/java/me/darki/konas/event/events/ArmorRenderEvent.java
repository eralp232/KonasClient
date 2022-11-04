package me.darki.konas.event.events;

import net.minecraft.inventory.EntityEquipmentSlot;

public class ArmorRenderEvent extends CancellableEvent {
    private static ArmorRenderEvent INSTANCE = new ArmorRenderEvent();

    private EntityEquipmentSlot slot;

    public static ArmorRenderEvent get(EntityEquipmentSlot slot) {
        INSTANCE.setCancelled(false);
        INSTANCE.slot = slot;
        return INSTANCE;
    }

    public EntityEquipmentSlot getSlot() {
        return slot;
    }
}
