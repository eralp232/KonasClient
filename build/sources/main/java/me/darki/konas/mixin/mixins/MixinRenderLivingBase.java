package me.darki.konas.mixin.mixins;

import cookiedragon.eventsystem.EventDispatcher;
import me.darki.konas.event.events.RenderEntityModelEvent;
import me.darki.konas.event.events.RenderNameEvent;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.module.modules.render.Chams;
import me.darki.konas.module.modules.render.ESP;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {RenderLivingBase.class}, priority = Integer.MAX_VALUE)
public abstract class MixinRenderLivingBase<T extends EntityLivingBase> extends Render<T> {

    protected MixinRenderLivingBase(RenderManager renderManager) {
        super(renderManager);
    }

    @Shadow protected abstract void renderModel(T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor);

    @Shadow protected ModelBase mainModel;

    /*@Inject(method = "doRender", at = @At("HEAD"))
    private <T extends EntityLivingBase> void injectChamsPre(final T a, final double b, final double c, final double d, final float e, final float f, final CallbackInfo g) {
        if (ModuleManager.getModuleByName("Chams").isEnabled()) {
            GL11.glEnable(32823);
            GL11.glPolygonOffset(1.0f, -1000000.0f);
        }
    }

    @Inject(method = "doRender", at = @At("RETURN"))
    private <T extends EntityLivingBase> void injectChamsPost(final T a, final double b, final double c, final double d, final float e, final float f, final CallbackInfo g) {
        if (ModuleManager.getModuleByName("Chams").isEnabled()) {
            GL11.glPolygonOffset(1.0f, 1000000.0f);
            GL11.glDisable(32823);
        }
    }*/

    @Shadow protected abstract boolean isVisible(T p_193115_1_);

    @Redirect(method = "doRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderLivingBase;renderModel(Lnet/minecraft/entity/EntityLivingBase;FFFFFF)V"))
    public void outlineRedirect(RenderLivingBase renderLivingBase, T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
        RenderEntityModelEvent.Pre pre = new RenderEntityModelEvent.Pre(renderLivingBase.getMainModel(), entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
        if (!ESP.isShading) {
            EventDispatcher.Companion.dispatch(pre);
        }

        if (!pre.isCancelled()) {
            if (ModuleManager.getModuleByClass(Chams.class).isEnabled() || ModuleManager.getModuleByClass(ESP.class).isEnabled()) {
                boolean flag = isVisible(entity);
                boolean flag1 = !flag && !entity.isInvisibleToPlayer(Minecraft.getMinecraft().player);

                if (flag || flag1) {
                    if (!bindEntityTexture(entity)) {
                        if (!ESP.isShading) {
                            RenderEntityModelEvent.Post post = new RenderEntityModelEvent.Post(renderLivingBase.getMainModel(), entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
                            EventDispatcher.Companion.dispatch(post);
                        }
                        return;
                    }

                    if (flag1) {
                        GlStateManager.enableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
                    }

                    if ((ModuleManager.getModuleByClass(Chams.class).isEnabled() || Chams.hackyFix) && !ESP.isShading) {
                        if (Chams.onRenderEntity(mainModel, entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor)) {
                            if (!ESP.isShading) {
                                RenderEntityModelEvent.Post post = new RenderEntityModelEvent.Post(renderLivingBase.getMainModel(), entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
                                EventDispatcher.Companion.dispatch(post);
                            }
                            return;
                        }
                    }

                    this.mainModel.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);

                    if (flag1) {
                        GlStateManager.disableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
                    }
                }
            } else {
                renderModel(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
            }
        }

        if (!ESP.isShading) {
            RenderEntityModelEvent.Post post = new RenderEntityModelEvent.Post(renderLivingBase.getMainModel(), entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
            EventDispatcher.Companion.dispatch(post);
        }
    }

    @Inject(method = "doRender", at = @At("RETURN"))
    public void injectReturn(T entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
        if(entity instanceof EntityPlayer) {
            ESP.renderSkeleton((EntityPlayer) entity, (ModelPlayer) mainModel, partialTicks);
        }
    }

    @Inject(method = "renderName", at = @At("HEAD"), cancellable = true)
    private void renderName(EntityLivingBase entity, double x, double y, double z, CallbackInfo ci) {
        if (ESP.isShading) {
            ci.cancel();
            return;
        }
        final RenderNameEvent event = new RenderNameEvent(entity);
        EventDispatcher.Companion.dispatch(event);
        if (event.isCancelled()) ci.cancel();
    }
}