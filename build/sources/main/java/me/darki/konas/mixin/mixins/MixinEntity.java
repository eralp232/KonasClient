package me.darki.konas.mixin.mixins;

import cookiedragon.eventsystem.EventDispatcher;
import me.darki.konas.event.events.EntityCollisionEvent;
import me.darki.konas.event.events.TurnEvent;
import me.darki.konas.event.events.WebEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {Entity.class}, priority = Integer.MAX_VALUE)
public abstract class MixinEntity {
    @Shadow
    private int entityId;

    @Shadow
    protected boolean isInWeb;

    @Shadow
    public void move(MoverType type, double x, double y, double z) {}

    @Shadow
    public double motionX;

    @Shadow
    public double motionY;

    @Shadow
    public double motionZ;

    @Shadow
    public abstract boolean equals(Object paramObject);

    @Shadow public abstract int getEntityId();

    @ModifyVariable(method = "addVelocity", at = @At(value = "HEAD"), ordinal = 0)
    private double modifyVariable1(double x) {
        EntityCollisionEvent event = EntityCollisionEvent.get((Entity)(Object)this, x, EntityCollisionEvent.Type.HORIZONTAL);
        EventDispatcher.Companion.dispatch(event);
        return event.getCoordinate();
    }

    @ModifyVariable(method = "addVelocity", at = @At(value = "HEAD"), ordinal = 1)
    private double modifyVariable2(double y) {
        EntityCollisionEvent event = EntityCollisionEvent.get((Entity)(Object)this, y, EntityCollisionEvent.Type.VERTICAL);
        EventDispatcher.Companion.dispatch(event);
        return event.getCoordinate();
    }

    @ModifyVariable(method = "addVelocity", at = @At(value = "HEAD"), ordinal = 2)
    private double modifyVariable3(double z) {
        EntityCollisionEvent event = EntityCollisionEvent.get((Entity)(Object)this, z, EntityCollisionEvent.Type.HORIZONTAL);
        EventDispatcher.Companion.dispatch(event);
        return event.getCoordinate();
    }

    @Inject(method = "move", at = @At("HEAD"))
    public void injectWebEvent(MoverType type, double x, double y, double z, CallbackInfo ci) {
        if(isInWeb) {
            WebEvent event = new WebEvent();
            EventDispatcher.Companion.dispatch(event);
            if(event.isCancelled()) isInWeb = false;
        }
    }

    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    public void onTurn(float yaw, float pitch, CallbackInfo ci) {
        TurnEvent event = new TurnEvent(yaw, pitch);
        EventDispatcher.Companion.dispatch(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
