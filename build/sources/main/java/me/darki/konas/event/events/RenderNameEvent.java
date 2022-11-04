package me.darki.konas.event.events;

import net.minecraft.entity.EntityLivingBase;

public class RenderNameEvent extends CancellableEvent {
    private EntityLivingBase nameEntity;

    public RenderNameEvent(EntityLivingBase nameEntity) {
        this.nameEntity = nameEntity;
    }

    public EntityLivingBase getNameEntity() {
        return this.nameEntity;
    }
}
