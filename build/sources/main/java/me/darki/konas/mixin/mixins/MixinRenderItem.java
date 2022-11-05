package me.darki.konas.mixin.mixins;

import me.darki.konas.module.ModuleManager;
import me.darki.konas.module.modules.render.CustomEnchants;
import me.darki.konas.module.modules.render.ESP;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderItem.class)
public class MixinRenderItem {
    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemCameraTransforms$TransformType;)V", at = @At("HEAD"), cancellable = true)
    public void onRenderItemFixed(ItemStack stack, ItemCameraTransforms.TransformType cameraTransformType, CallbackInfo ci) {
        if (ESP.isShading) {
            ci.cancel();
        }
    }
    
    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/client/renderer/block/model/ItemCameraTransforms$TransformType;Z)V", at = @At("HEAD"), cancellable = true)
    public void onRenderItem(ItemStack stack, EntityLivingBase entitylivingbaseIn, ItemCameraTransforms.TransformType transform, boolean leftHanded, CallbackInfo ci) {
        if (ESP.isShading) {
            ci.cancel();
        }
    }

    @ModifyArg(method = "renderEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderItem;renderModel(Lnet/minecraft/client/renderer/block/model/IBakedModel;I)V"), index = 1)
    public int changeColor(int in) {
        if (ModuleManager.getModuleByClass(CustomEnchants.class).isEnabled()) {
            return CustomEnchants.enchantColor.getValue().getColor();
        } else {
            return in;
        }
    }
}
