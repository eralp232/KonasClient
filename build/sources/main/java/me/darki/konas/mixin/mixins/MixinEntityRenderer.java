package me.darki.konas.mixin.mixins;

import cookiedragon.eventsystem.EventDispatcher;
import me.darki.konas.event.events.*;
import me.darki.konas.module.modules.render.Viewport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

/**
 * @author cats
 * Entity renderer mixin, made for the Render3D event for ESP, but likely will be home to more mixins as time continues
 */
@Mixin(value = EntityRenderer.class, priority = Integer.MAX_VALUE)
public class MixinEntityRenderer {

    @Inject(method = "setupCameraTransform", at = @At(value = "HEAD"))
    private void setupCameraTransformPre(float partialTicks, int pass, CallbackInfo ci) {
        ZoomEvent.Pre event = new ZoomEvent.Pre();
        EventDispatcher.Companion.dispatch(event);
    }

    @Inject(method = "setupCameraTransform", at = @At(value = "TAIL"))
    private void setupCameraTransformPost(float partialTicks, int pass, CallbackInfo ci) {
        ZoomEvent.Post event = new ZoomEvent.Post();
        EventDispatcher.Companion.dispatch(event);
    }

    @Inject(method = "renderWorldPass", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;clear(I)V", ordinal = 1, shift = At.Shift.BEFORE))
    private void renderWorldPass(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        if (Display.isActive() || Display.isVisible()) {
            Render3DEvent event = Render3DEvent.get(partialTicks);
            EventDispatcher.Companion.dispatch(event);
            GlStateManager.resetColor();
            GlStateManager.color(1F, 1F, 1F, 1F);
        }
    }

    @Inject(method = "renderWorldPass", at = @At(value = "TAIL"))
    private void renderWorldPassPost(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        if (Display.isActive() || Display.isVisible()) {
            PostProccessRenderEvent pEvent = new PostProccessRenderEvent();
            EventDispatcher.Companion.dispatch(pEvent);
        }
    }

    @Inject(method = "hurtCameraEffect", at = @At("HEAD"), cancellable = true)
    public void hurtCameraEffect(float ticks, CallbackInfo info) {
        HurtCameraEvent event = new HurtCameraEvent();
        EventDispatcher.Companion.dispatch(event);
        if(event.isCancelled()) info.cancel();
    }

    @Inject(method = "isDrawBlockOutline", at = @At("HEAD"), cancellable = true)
    public void injectIsDrawBlockOutline(CallbackInfoReturnable<Boolean> cir) {
        DrawBlockOutlineEvent event = DrawBlockOutlineEvent.get();
        EventDispatcher.Companion.dispatch(event);
        if(event.isCancelled()) cir.setReturnValue(false);
    }

    @Inject(method = "getMouseOver", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;getEntitiesInAABBexcluding(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;Lcom/google/common/base/Predicate;)Ljava/util/List;"), cancellable = true)
    public void injectTraceEvent(float partialTicks, CallbackInfo ci) {
        final TraceEntityEvent event = new TraceEntityEvent();
        EventDispatcher.Companion.dispatch(event);
        if (event.isCancelled()) ci.cancel();
    }

    @ModifyVariable(method = {"orientCamera"}, ordinal = 3, at = @At(value = "STORE", ordinal = 0), require = 1)
    public double orientCameraDist(double distance) {
        OrientCameraEvent.Pre event = new OrientCameraEvent.Pre(distance);
        EventDispatcher.Companion.dispatch(event);

        if (event.isCancelled()) {
            return event.getDistance();
        } else {
            return distance;
        }

    }

    @ModifyVariable(method = {"orientCamera"}, ordinal = 7, at = @At(value = "STORE", ordinal = 0), require = 1)
    public double orientCamera(double distance) {
        OrientCameraEvent.Post event = new OrientCameraEvent.Post(distance);
        EventDispatcher.Companion.dispatch(event);

        if (event.isCancelled()) {
            return event.getDistance();
        } else {
            return distance;
        }
    }

    @ModifyVariable(method = "getMouseOver", ordinal = 0, index = 2, name = "entity", at = @At(value = "STORE", ordinal = 0))
    private Entity injectMouseOver(Entity entity) {
        GetRenderEntityForMouseOverEvent event = new GetRenderEntityForMouseOverEvent(entity);
        EventDispatcher.Companion.dispatch(event);
        return event.getEntity();
    }


    @Inject(method = {"renderHand"}, at = {@At(value = "HEAD")}, cancellable = true)
    private void onRenderHand(CallbackInfo ci) {
        RenderHandEvent event = new RenderHandEvent();
        EventDispatcher.Companion.dispatch(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = {"updateCameraAndRender"}, at = {@At(value = "HEAD")})
    private void onUpdateCameraAndRenderPre(float partialTicks, long nanoTime, CallbackInfo ci) {
        UpdateCameraAndRenderEvent.Pre event = new UpdateCameraAndRenderEvent.Pre();
        EventDispatcher.Companion.dispatch(event);
    }

    @Inject(method = {"updateCameraAndRender"}, at = {@At(value = "TAIL")})
    private void onUpdateCameraAndRenderPost(float partialTicks, long nanoTime, CallbackInfo ci) {
        UpdateCameraAndRenderEvent.Post event = new UpdateCameraAndRenderEvent.Post();
        EventDispatcher.Companion.dispatch(event);
    }

    @Redirect(method = "setupCameraTransform", at = @At(value = "INVOKE", target = "Lorg/lwjgl/util/glu/Project;gluPerspective(FFFF)V"))
    private void projectInject0(float fovy, float aspect, float zNear, float zFar) {
        Viewport.project(fovy, aspect, zNear, zFar);
    }

    @Redirect(method = "renderHand", at = @At(value = "INVOKE", target = "Lorg/lwjgl/util/glu/Project;gluPerspective(FFFF)V"))
    private void projectInject1(float fovy, float aspect, float zNear, float zFar) {
        Viewport.project(fovy, aspect, zNear, zFar, true);
    }

    @Redirect(method = "renderWorldPass", at = @At(value = "INVOKE", target = "Lorg/lwjgl/util/glu/Project;gluPerspective(FFFF)V"))
    private void projectInject2(float fovy, float aspect, float zNear, float zFar) {
        Viewport.project(fovy, aspect, zNear, zFar);
    }

    @Redirect(method = "renderCloudsCheck", at = @At(value = "INVOKE", target = "Lorg/lwjgl/util/glu/Project;gluPerspective(FFFF)V"))
    private void projectInject3(float fovy, float aspect, float zNear, float zFar) {
        Viewport.project(fovy, aspect, zNear, zFar);
    }

    @Redirect(method = "getMouseOver", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getRenderViewEntity()Lnet/minecraft/entity/Entity;"))
    private Entity redirectMouseOver(Minecraft mc) {
        FreecamEvent event = new FreecamEvent();
        EventDispatcher.Companion.dispatch(event);
        if (event.isCancelled()) {
            if (Keyboard.isKeyDown(Keyboard.KEY_LMENU)) {
                return mc.player;
            }
        }
        return mc.getRenderViewEntity();
    }

    @Redirect(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;turn(FF)V"))
    private void redirectTurn(EntityPlayerSP entityPlayerSP, float yaw, float pitch) {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            FreecamEvent event = new FreecamEvent();
            EventDispatcher.Companion.dispatch(event);
            if (event.isCancelled()) {
                if (Keyboard.isKeyDown(Keyboard.KEY_LMENU)) {
                    mc.player.turn(yaw, pitch);
                } else {
                    Objects.requireNonNull(mc.getRenderViewEntity(), "Render Entity").turn(yaw, pitch);
                }
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        entityPlayerSP.turn(yaw, pitch);
    }

    @Redirect(method = "renderWorldPass", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;isSpectator()Z"))
    public boolean redirectIsSpectator(EntityPlayerSP entityPlayerSP) {
        FreecamEvent event = new FreecamEvent();
        EventDispatcher.Companion.dispatch(event);
        if (event.isCancelled()) return true;
        if (entityPlayerSP != null) {
            return entityPlayerSP.isSpectator();
        }
        return false;
    }
}
