package me.darki.konas.mixin.mixins;

import cookiedragon.eventsystem.EventDispatcher;
import me.darki.konas.event.events.FreecamEntityEvent;
import me.darki.konas.event.events.PotionRenderHUDEvent;
import me.darki.konas.event.events.RenderAttackIndicatorEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngame.class)
public class MixinGuiIngame {

    @Inject(method = "renderGameOverlay", at = @At("TAIL"), cancellable = true)
    public void renderIngameOverlay(final float partialTicks, final CallbackInfo info) {
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow("Konas", 3f, 5f, 0xFFFFFF);
    }

    @Inject(method = "renderPotionEffects", at = @At("HEAD"), cancellable = true)
    protected void renderPotionEffectsHook(ScaledResolution scaledRes, CallbackInfo info) {
        PotionRenderHUDEvent event = new PotionRenderHUDEvent();
        EventDispatcher.Companion.dispatch(event);
        if (event.isCancelled()) info.cancel();
    }

    @Inject(method = "renderAttackIndicator", at = @At("HEAD"), cancellable = true)
    public void onRenderAttackIndicator(float partialTicks, ScaledResolution p_184045_2_, CallbackInfo ci) {
        RenderAttackIndicatorEvent event = new RenderAttackIndicatorEvent();
        EventDispatcher.Companion.dispatch(event);
        if (event.isCancelled()) ci.cancel();
    }

    @Redirect(method = "renderGameOverlay", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;player:Lnet/minecraft/client/entity/EntityPlayerSP;"))
    private EntityPlayerSP redirectOverlayPlayer(Minecraft mc) {
        FreecamEntityEvent event = new FreecamEntityEvent(mc.player);
        EventDispatcher.Companion.dispatch(event);
        if (event.getEntity() instanceof EntityPlayerSP) {
            return (EntityPlayerSP) event.getEntity();
        }
        return mc.player;
    }

    @Redirect(method = "renderPotionEffects", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;player:Lnet/minecraft/client/entity/EntityPlayerSP;"))
    private EntityPlayerSP redirectPotionPlayer(Minecraft mc) {
        FreecamEntityEvent event = new FreecamEntityEvent(mc.player);
        EventDispatcher.Companion.dispatch(event);
        if (event.getEntity() instanceof EntityPlayerSP) {
            return (EntityPlayerSP) event.getEntity();
        }
        return mc.player;
    }


}
