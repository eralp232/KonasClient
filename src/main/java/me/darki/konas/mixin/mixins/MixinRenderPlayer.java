package me.darki.konas.mixin.mixins;

import cookiedragon.eventsystem.EventDispatcher;
import me.darki.konas.event.events.FreecamEvent;
import me.darki.konas.event.events.RenderArmEvent;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.module.modules.render.Chams;
import me.darki.konas.module.modules.render.PacketRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderPlayer;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.lwjgl.opengl.GL11.GL_FILL;

@Mixin(RenderPlayer.class)
public abstract class MixinRenderPlayer {

    @Shadow public abstract ModelPlayer getMainModel();

    @Shadow protected abstract void setModelVisibilities(AbstractClientPlayer clientPlayer);

    private float
            renderPitch,
            renderYaw,
            renderHeadYaw,
            prevRenderHeadYaw,
            lastRenderHeadYaw = 0,
            prevRenderPitch,
            lastRenderPitch = 0;

    @Inject(method = "doRender", at = @At("HEAD"))
    private void rotateBegin(AbstractClientPlayer entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
        if (ModuleManager.getModuleByClass(PacketRender.class).isEnabled()
                && entity == Minecraft.getMinecraft().player) {
            prevRenderHeadYaw = entity.prevRotationYawHead;
            prevRenderPitch = entity.prevRotationPitch;
            renderPitch = entity.rotationPitch;
            renderYaw = entity.rotationYaw;
            renderHeadYaw = entity.rotationYawHead;
            entity.rotationPitch = PacketRender.getPitch();
            entity.prevRotationPitch = lastRenderPitch;
            entity.rotationYaw = PacketRender.getYaw();
            entity.rotationYawHead = PacketRender.getYaw();
            entity.prevRotationYawHead = lastRenderHeadYaw;
        }
    }

    @Inject(method = "doRender", at = @At("RETURN"))
    private void rotateEnd(AbstractClientPlayer entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
        if (ModuleManager.getModuleByClass(PacketRender.class).isEnabled()
                && entity == Minecraft.getMinecraft().player) {
            lastRenderHeadYaw = entity.rotationYawHead;
            lastRenderPitch = entity.rotationPitch;
            entity.rotationPitch = renderPitch;
            entity.rotationYaw = renderYaw;
            entity.rotationYawHead = renderHeadYaw;
            entity.prevRotationYawHead = prevRenderHeadYaw;
            entity.prevRotationPitch = prevRenderPitch;
        }
    }

    private boolean preCancelled = false;

    @Inject(method = "renderRightArm", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/ModelPlayer;swingProgress:F", opcode = 181), cancellable = true)
    public void renderRightArmBegin(AbstractClientPlayer clientPlayer, CallbackInfo ci) {
        if (clientPlayer == Minecraft.getMinecraft().player) {
            RenderArmEvent event = new RenderArmEvent();
            EventDispatcher.Companion.dispatch(event);
            if (event.isCancelled()) {
                GL11.glPushAttrib(1048575);
                GL11.glDisable(3008);
                GL11.glDisable(3553);
                GL11.glDisable(2896);
                GL11.glEnable(3042);
                GL11.glBlendFunc(770, 771);
                GL11.glLineWidth(1.5F);
                GL11.glEnable(2960);
                GL11.glEnable(10754);
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
                GL11.glColor4f(event.getRed() / 255.0F, event.getGreen() / 255.0F, event.getBlue() / 255.0F, event.getAlpha() / 255.0F);
                preCancelled = true;
            }
        }
    }

    @Inject(method = "renderRightArm", at = @At("RETURN"), cancellable = true)
    public void renderRightArmReturn(AbstractClientPlayer clientPlayer, CallbackInfo ci) {
        if (clientPlayer == (Minecraft.getMinecraft()).player && preCancelled) {
            GL11.glEnable(3042);
            GL11.glEnable(2896);
            GL11.glEnable(3553);
            GL11.glEnable(3008);
            GL11.glPopAttrib();
            preCancelled = false;
        }
        if (Chams.hGlintMode.getValue() != Chams.GlintMode.NONE && ModuleManager.getModuleByClass(Chams.class).isEnabled()) {
            ModelPlayer modelplayer = this.getMainModel();
            this.setModelVisibilities(clientPlayer);
            GlStateManager.enableBlend();
            modelplayer.swingProgress = 0.0F;
            modelplayer.isSneak = false;
            modelplayer.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, clientPlayer);

            GlStateManager.pushMatrix();
            Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(Chams.hGlintMode.getValue() == Chams.GlintMode.CUSTOM ? Chams.LIGHTNING_TEXTURE : Chams.ENCHANTED_ITEM_GLINT_RES);
            GL11.glPushAttrib(1048575);
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL_FILL);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glColor4f(Chams.hGlintColor.getValue().getRed() / 255F, Chams.hGlintColor.getValue().getGreen() / 255F, Chams.hGlintColor.getValue().getBlue() / 255F, Chams.hGlintColor.getValue().getAlpha() / 255F);
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);

            for (int i = 0; i < 2; ++i)
            {
                GlStateManager.matrixMode(GL11.GL_TEXTURE);
                GlStateManager.loadIdentity();
                float tScale = 0.33333334F * Chams.hGlintScale.getValue();
                GlStateManager.scale(tScale, tScale, tScale);
                GlStateManager.rotate(30.0F - (float)i * 60.0F, 0.0F, 0.0F, 1.0F);
                GlStateManager.translate(0.0F, (clientPlayer.ticksExisted + Minecraft.getMinecraft().getRenderPartialTicks()) * (0.001F + (float)i * 0.003F) * Chams.hGlintSpeed.getValue(), 0.0F);
                GlStateManager.matrixMode(GL11.GL_MODELVIEW);
                modelplayer.bipedRightArm.rotateAngleX = 0.0F;
                modelplayer.bipedRightArm.render(0.0625F);
                modelplayer.bipedRightArmwear.rotateAngleX = 0.0F;
                modelplayer.bipedRightArmwear.render(0.0625F);
            }

            GlStateManager.matrixMode(5890);
            GlStateManager.loadIdentity();
            GlStateManager.matrixMode(5888);

            GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GL11.glScalef(1F / Chams.scale.getValue(), 1F / Chams.scale.getValue(), 1F / Chams.scale.getValue());
            GL11.glPopAttrib();
            GL11.glPopMatrix();

            GlStateManager.disableBlend();
        }
    }

    @Inject(method = "renderLeftArm", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/ModelPlayer;swingProgress:F", opcode = 181), cancellable = true)
    public void renderLeftArmBegin(AbstractClientPlayer clientPlayer, CallbackInfo ci) {
        if (clientPlayer == Minecraft.getMinecraft().player) {
            RenderArmEvent event = new RenderArmEvent();
            EventDispatcher.Companion.dispatch(event);
            if (event.isCancelled()) {
                GL11.glPushAttrib(1048575);
                GL11.glDisable(3008);
                GL11.glDisable(3553);
                GL11.glDisable(2896);
                GL11.glEnable(3042);
                GL11.glBlendFunc(770, 771);
                GL11.glLineWidth(1.5F);
                GL11.glEnable(2960);
                GL11.glEnable(10754);
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
                GL11.glColor4f(event.getRed() / 255.0F, event.getGreen() / 255.0F, event.getBlue() / 255.0F, event.getAlpha() / 255.0F);
                preCancelled = true;
            }
        }
    }

    @Inject(method = "renderLeftArm", at = @At("RETURN"), cancellable = true)
    public void renderLeftArmReturn(AbstractClientPlayer clientPlayer, CallbackInfo ci) {
        if (clientPlayer == (Minecraft.getMinecraft()).player && preCancelled) {
            GL11.glEnable(3042);
            GL11.glEnable(2896);
            GL11.glEnable(3553);
            GL11.glEnable(3008);
            GL11.glPopAttrib();
            preCancelled = false;
        }
    }

    @Redirect(method = "doRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/AbstractClientPlayer;isUser()Z"))
    private boolean isUserRedirect(AbstractClientPlayer abstractClientPlayer) {
        Minecraft mc = Minecraft.getMinecraft();
        FreecamEvent event = new FreecamEvent();
        EventDispatcher.Companion.dispatch(event);
        if (event.isCancelled()) {
            return abstractClientPlayer.isUser() && abstractClientPlayer == mc.getRenderViewEntity();
        }
        return abstractClientPlayer.isUser();
    }

}
