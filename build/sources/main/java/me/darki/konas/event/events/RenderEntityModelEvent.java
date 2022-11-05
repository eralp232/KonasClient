package me.darki.konas.event.events;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;

public class RenderEntityModelEvent extends CancellableEvent {

    private final ModelBase modelBase;
    private final Entity entityIn;
    private final float limbSwing;
    private final float limbSwingAmount;
    private final float ageInTicks;
    private final float netHeadYaw;
    private final float headPitch;
    private final float scale;

    public RenderEntityModelEvent(ModelBase modelBase, Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        this.modelBase = modelBase;
        this.entityIn = entityIn;
        this.limbSwing = limbSwing;
        this.limbSwingAmount = limbSwingAmount;
        this.ageInTicks = ageInTicks;
        this.netHeadYaw = netHeadYaw;
        this.headPitch = headPitch;
        this.scale = scale;
    }

    public static class Pre extends RenderEntityModelEvent {
        public Pre(ModelBase modelBase, Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            super(modelBase, entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        }
    }

    public static class Post extends RenderEntityModelEvent {
        public Post(ModelBase modelBase, Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            super(modelBase, entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        }
    }

    public ModelBase getModelBase() {
        return modelBase;
    }

    public Entity getEntityIn() {
        return entityIn;
    }

    public float getLimbSwing() {
        return limbSwing;
    }

    public float getLimbSwingAmount() {
        return limbSwingAmount;
    }

    public float getAgeInTicks() {
        return ageInTicks;
    }

    public float getNetHeadYaw() {
        return netHeadYaw;
    }

    public float getHeadPitch() {
        return headPitch;
    }

    public float getScale() {
        return scale;
    }
}
