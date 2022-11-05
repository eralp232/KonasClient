package me.darki.konas.mixin.mixins;

import cookiedragon.eventsystem.EventDispatcher;
import me.darki.konas.event.events.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer {

    @Shadow protected abstract void transformFirstPerson(EnumHandSide hand, float p_187453_2_);

    @Shadow protected abstract void transformSideFirstPerson(EnumHandSide hand, float p_187459_2_);

    @Inject(method = "transformSideFirstPerson", at = @At("HEAD"), cancellable = true)
    public void transformInject(EnumHandSide hand, float y, CallbackInfo ci) {
        int i = hand == EnumHandSide.RIGHT ? 1 : -1;
        ItemTransformEvent event = new ItemTransformEvent((float)i * 0.56F, -0.52F + y * -0.6F, -0.72F, 1F, 1F, 1F, hand == EnumHandSide.LEFT ? ItemTransformEvent.Type.OFFHAND : ItemTransformEvent.Type.MAINHAND);
        EventDispatcher.Companion.dispatch(event);
        if (event.isCancelled()) {
            ci.cancel();
            GlStateManager.translate(event.getX(), event.getY(), event.getZ());
            GlStateManager.scale(event.getScaleX(), event.getScaleY(), event.getScaleZ());
            GlStateManager.rotate(event.getPitch(), 1F, 0F, 0F);
            GlStateManager.rotate(event.getYaw(), 0F, 1F, 0F);
            GlStateManager.rotate(event.getRoll(), 0F, 0F, 1F);
        }
    }

    @Inject(method = {"updateEquippedItem"}, at = {@At(value = "HEAD")}, cancellable = true)
    private void onUpdateEquippedItem(CallbackInfo ci) {
        UpdateEquippedItemEvent event = new UpdateEquippedItemEvent();
        EventDispatcher.Companion.dispatch(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "transformEatFirstPerson", at = @At("HEAD"), cancellable = true)
    public void transformEatFirstPerson(float p_187454_1_, EnumHandSide hand, ItemStack stack, CallbackInfo ci){
        TransformEatFirstPersonEvent event = new TransformEatFirstPersonEvent(p_187454_1_, hand, stack);
        EventDispatcher.Companion.dispatch(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderItemInFirstPerson(F)V", at = @At("HEAD"), cancellable = true)
    private void renderItemInFirstPerson(float partialTicks, CallbackInfo ci) {
        RenderItemOverlayEvent event = new RenderItemOverlayEvent();
        EventDispatcher.Companion.dispatch(event);
        if (event.isCancelled()) ci.cancel();
    }

    @Redirect(method = "setLightmap", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;player:Lnet/minecraft/client/entity/EntityPlayerSP;"))
    private EntityPlayerSP redirectLightmapPlayer(Minecraft mc) {
        FreecamEntityEvent event = new FreecamEntityEvent(mc.player);
        EventDispatcher.Companion.dispatch(event);
        return (EntityPlayerSP) event.getEntity();
    }

    @Redirect(method = "rotateArm", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;player:Lnet/minecraft/client/entity/EntityPlayerSP;"))
    private EntityPlayerSP rotateArmPlayer(Minecraft mc) {
        FreecamEntityEvent event = new FreecamEntityEvent(mc.player);
        EventDispatcher.Companion.dispatch(event);
        return (EntityPlayerSP) event.getEntity();
    }

    @Redirect(method = "renderItemInFirstPerson(F)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;player:Lnet/minecraft/client/entity/EntityPlayerSP;"))
    private EntityPlayerSP redirectPlayer(Minecraft mc) {
        FreecamEntityEvent event = new FreecamEntityEvent(mc.player);
        EventDispatcher.Companion.dispatch(event);
        return (EntityPlayerSP) event.getEntity();
    }

    @Inject(method = "renderOverlays", at = @At("HEAD"), cancellable = true)
    private void renderOverlaysInject(float partialTicks, CallbackInfo ci) {
        FreecamEvent event = new FreecamEvent();
        EventDispatcher.Companion.dispatch(event);
        if(event.isCancelled()) ci.cancel();
    }

    @Redirect(method = "renderOverlays", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;player:Lnet/minecraft/client/entity/EntityPlayerSP;"))
    private EntityPlayerSP renderOverlaysPlayer(Minecraft mc) {
        FreecamEntityEvent event = new FreecamEntityEvent(mc.player);
        EventDispatcher.Companion.dispatch(event);
        return (EntityPlayerSP) event.getEntity();
    }

}