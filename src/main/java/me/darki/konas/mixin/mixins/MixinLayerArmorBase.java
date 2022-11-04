package me.darki.konas.mixin.mixins;

import cookiedragon.eventsystem.EventDispatcher;
import me.darki.konas.event.events.ArmorRenderEvent;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.module.modules.render.CustomEnchants;
import me.darki.konas.module.modules.render.ESP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LayerArmorBase.class)
public class MixinLayerArmorBase {

    @Inject(method = "renderArmorLayer", at=@At("HEAD"), cancellable = true)
    public void onRenderArmorLayer(EntityLivingBase entityLivingBaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, EntityEquipmentSlot slotIn, CallbackInfo ci) {
        if (ESP.isShading) {
            ci.cancel();
            return;
        }
        ArmorRenderEvent event = ArmorRenderEvent.get(slotIn);
        EventDispatcher.Companion.dispatch(event);
        if(event.isCancelled()) {
            ci.cancel();
        }
    }

    @Redirect(method = "renderEnchantedGlint", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;color(FFFF)V"))
    private static void onRenderEnchantedGlint(float colorRed, float colorGreen, float colorBlue, float colorAlpha) {
        if (colorBlue == 0.608F && ModuleManager.getModuleByClass(CustomEnchants.class).isEnabled()) {
            GlStateManager.color(CustomEnchants.enchantColor.getValue().getRed() / 255F, CustomEnchants.enchantColor.getValue().getGreen() / 255F, CustomEnchants.enchantColor.getValue().getBlue() / 255F, CustomEnchants.enchantColor.getValue().getAlpha() / 255F);
        } else {
            GlStateManager.color(colorRed, colorGreen, colorBlue, colorAlpha);
        }
    }
}
