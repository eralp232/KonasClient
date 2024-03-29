package me.darki.konas.mixin.mixins;

import cookiedragon.eventsystem.EventDispatcher;
import me.darki.konas.event.events.RenderEntityEvent;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderManager.class)
public class MixinRenderManager {

    @Inject(method = "renderEntity", at = @At(value = "INVOKE"), cancellable = true)
    public void onRenderEntityPre(Entity entityIn, double x, double y, double z, float yaw, float partialTicks, boolean p_188391_10_, CallbackInfo ci) {
        RenderEntityEvent.Pre pre = new RenderEntityEvent.Pre(entityIn, x, y, z, yaw, partialTicks);
        EventDispatcher.Companion.dispatch(pre);
        if (pre.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderEntity", at = @At(value = "TAIL"))
    public void onRenderEntityPost(Entity entityIn, double x, double y, double z, float yaw, float partialTicks, boolean p_188391_10_, CallbackInfo ci) {
        RenderEntityEvent.Post post = new RenderEntityEvent.Post(entityIn, x, y, z, yaw, partialTicks);
        EventDispatcher.Companion.dispatch(post);
    }


}
