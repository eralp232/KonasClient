package me.darki.konas.mixin.mixins;

import me.darki.konas.module.ModuleManager;
import me.darki.konas.module.modules.render.Chams;
import me.darki.konas.module.modules.render.ESP;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderEnderCrystal;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.lwjgl.opengl.GL11.*;

@Mixin(value = RenderEnderCrystal.class, priority = 1069)
public abstract class MixinRenderEnderCrystal extends Render<EntityEnderCrystal> {
    @Shadow
    public ModelBase modelEnderCrystal;

    @Shadow
    public ModelBase modelEnderCrystalNoBase;

    @Final
    @Shadow
    private static ResourceLocation ENDER_CRYSTAL_TEXTURES;

    protected MixinRenderEnderCrystal(RenderManager renderManager) {
        super(renderManager);
    }

    @Shadow
    public abstract void doRender(EntityEnderCrystal entity, double x, double y, double z, float entityYaw, float partialTicks);

    @Redirect(method = {"doRender(Lnet/minecraft/entity/item/EntityEnderCrystal;DDDFF)V"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelBase;render(Lnet/minecraft/entity/Entity;FFFFFF)V"))
    private void bottomRenderRedirect(ModelBase modelBase, Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (ModuleManager.getModuleByClass(Chams.class).isEnabled() && !ESP.isShading && Chams.crystalSetting.getValue()) {
            if (Chams.crystalSetting.getValue() && Chams.cRender.getValue()) {
                GL11.glScalef(Chams.scale.getValue(), Chams.scale.getValue(), Chams.scale.getValue());
                if (!Chams.cRenderDepth.getValue()) {
                    GL11.glPushAttrib(GL_ALL_ATTRIB_BITS);
                    GL11.glDepthMask(false);
                    GL11.glDisable(GL_DEPTH_TEST);
                }
                modelBase.render(entityIn, limbSwing, limbSwingAmount * Chams.spinSpeed.getValue(), ageInTicks * Chams.bounceSpeed.getValue(), netHeadYaw, headPitch, scale);
                if (!Chams.cRenderDepth.getValue()) {
                    GL11.glEnable(GL_DEPTH_TEST);
                    GL11.glDepthMask(true);
                    GL11.glPopAttrib();
                }
                GL11.glScalef(1F / Chams.scale.getValue(), 1F / Chams.scale.getValue(), 1F / Chams.scale.getValue());
            } else if (!Chams.crystalSetting.getValue()) {
                modelBase.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            }
        } else if (ModuleManager.getModuleByClass(Chams.class).isEnabled() && ESP.isShading && Chams.crystalSetting.getValue()) {
            GL11.glScalef(Chams.scale.getValue(), Chams.scale.getValue(), Chams.scale.getValue());
            modelBase.render(entityIn, limbSwing, limbSwingAmount * Chams.spinSpeed.getValue(), ageInTicks * Chams.bounceSpeed.getValue(), netHeadYaw, headPitch, scale);
            GL11.glScalef(1F / Chams.scale.getValue(), 1F / Chams.scale.getValue(), 1F / Chams.scale.getValue());
        } else {
            modelBase.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        }
    }

    @Inject(method = {"doRender(Lnet/minecraft/entity/item/EntityEnderCrystal;DDDFF)V"}, at = {@At("RETURN")}, cancellable = true)
    public void doRenderCrystal(EntityEnderCrystal entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
       if (ModuleManager.getModuleByClass(Chams.class).isEnabled() && !ESP.isShading && Chams.crystalSetting.getValue()) {
           if (Chams.cFill.getValue()) {
                final float f3 = entity.innerRotation + partialTicks;
                GlStateManager.pushMatrix();
                GlStateManager.translate(x, y, z);
                Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(ENDER_CRYSTAL_TEXTURES);
                float f4 = MathHelper.sin(f3 * 0.2f) / 2.0f + 0.5f;
                f4 += f4 * f4;
                GL11.glPushAttrib(1048575);
                GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
                GL11.glDisable(GL11.GL_TEXTURE_2D);
               if (Chams.cLighting.getValue()) {
                   GL11.glEnable(GL11.GL_LIGHTING);
               } else {
                   GL11.glDisable(GL11.GL_LIGHTING);
               }
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glEnable(GL11.GL_LINE_SMOOTH);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(770, 771);
                GL11.glColor4f(Chams.cFillColor.getValue().getRed() / 255F, Chams.cFillColor.getValue().getGreen() / 255F, Chams.cFillColor.getValue().getBlue() / 255F, Chams.cFillColor.getValue().getAlpha() / 255F);
                GL11.glScalef(Chams.scale.getValue(), Chams.scale.getValue(), Chams.scale.getValue());
               if (Chams.cFillDepth.getValue()) {
                   GL11.glDepthMask(true);
                   GL11.glEnable(GL_DEPTH_TEST);
               }
                if (entity.shouldShowBottom()) {
                    this.modelEnderCrystal.render(entity, 0.0f, f3 * 3.0f * Chams.spinSpeed.getValue(), f4 * 0.2f * Chams.bounceSpeed.getValue(), 0.0f, 0.0f, 0.0625f);
                }
                else {
                    this.modelEnderCrystalNoBase.render(entity, 0.0f, f3 * 3.0f * Chams.spinSpeed.getValue(), f4 * 0.2f * Chams.bounceSpeed.getValue(), 0.0f, 0.0f, 0.0625f);
                }
               if (Chams.cFillDepth.getValue()) {
                   GL11.glDisable(GL_DEPTH_TEST);
                   GL11.glDepthMask(false);
               }
                GL11.glScalef(1F / Chams.scale.getValue(), 1F / Chams.scale.getValue(), 1F / Chams.scale.getValue());
                GL11.glPopAttrib();
                GL11.glPopMatrix();
           }
           if (Chams.cOutline.getValue()) {
               final float f3 = entity.innerRotation + partialTicks;
               GlStateManager.pushMatrix();
               GlStateManager.translate(x, y, z);
               Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(ENDER_CRYSTAL_TEXTURES);
               float f4 = MathHelper.sin(f3 * 0.2f) / 2.0f + 0.5f;
               f4 += f4 * f4;
               GL11.glPushAttrib(1048575);
               GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL_LINE);
               GL11.glDisable(GL11.GL_TEXTURE_2D);
               GL11.glDisable(GL11.GL_LIGHTING);
               GL11.glDisable(GL11.GL_DEPTH_TEST);
               GL11.glEnable(GL11.GL_LINE_SMOOTH);
               GL11.glEnable(GL11.GL_BLEND);
               GL11.glBlendFunc(770, 771);
               GL11.glLineWidth(Chams.cWidth.getValue());
               GL11.glColor4f(Chams.cOutlineColor.getValue().getRed() / 255F, Chams.cOutlineColor.getValue().getGreen() / 255F, Chams.cOutlineColor.getValue().getBlue() / 255F, Chams.cOutlineColor.getValue().getAlpha() / 255F);
               GL11.glScalef(Chams.scale.getValue(), Chams.scale.getValue(), Chams.scale.getValue());
               if (Chams.cOutlineDepth.getValue()) {
                   GL11.glDepthMask(true);
                   GL11.glEnable(GL_DEPTH_TEST);
               }
               if (entity.shouldShowBottom()) {
                   this.modelEnderCrystal.render(entity, 0.0f, f3 * 3.0f * Chams.spinSpeed.getValue(), f4 * 0.2f * Chams.bounceSpeed.getValue(), 0.0f, 0.0f, 0.0625f);
               }
               else {
                   this.modelEnderCrystalNoBase.render(entity, 0.0f, f3 * 3.0f * Chams.spinSpeed.getValue(), f4 * 0.2f * Chams.bounceSpeed.getValue(), 0.0f, 0.0f, 0.0625f);
               }
               if (Chams.cOutlineDepth.getValue()) {
                   GL11.glDisable(GL_DEPTH_TEST);
                   GL11.glDepthMask(false);
               }
               GL11.glScalef(1F / Chams.scale.getValue(), 1F / Chams.scale.getValue(), 1F / Chams.scale.getValue());
               GL11.glPopAttrib();
               GL11.glPopMatrix();
           }
           if (Chams.cGlintMode.getValue() != Chams.GlintMode.NONE) {
               final float f3 = entity.innerRotation + partialTicks;
               GlStateManager.pushMatrix();
               GlStateManager.translate(x, y, z);
               Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(Chams.cGlintMode.getValue() == Chams.GlintMode.CUSTOM ? Chams.LIGHTNING_TEXTURE : Chams.ENCHANTED_ITEM_GLINT_RES);
               float f4 = MathHelper.sin(f3 * 0.2f) / 2.0f + 0.5f;
               f4 += f4 * f4;
               GL11.glPushAttrib(1048575);
               GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL_FILL);
               GL11.glDisable(GL11.GL_LIGHTING);
               GL11.glDisable(GL11.GL_DEPTH_TEST);
               GL11.glEnable(GL11.GL_BLEND);
               GL11.glColor4f(Chams.cGlintColor.getValue().getRed() / 255F, Chams.cGlintColor.getValue().getGreen() / 255F, Chams.cGlintColor.getValue().getBlue() / 255F, Chams.cGlintColor.getValue().getAlpha() / 255F);
               GL11.glScalef(Chams.scale.getValue(), Chams.scale.getValue(), Chams.scale.getValue());
               GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);

               for (int i = 0; i < 2; ++i)
               {
                   GlStateManager.matrixMode(GL11.GL_TEXTURE);
                   GlStateManager.loadIdentity();
                   float tScale = 0.33333334F * Chams.cGlintScale.getValue();
                   GlStateManager.scale(tScale, tScale, tScale);
                   GlStateManager.rotate(30.0F - (float)i * 60.0F, 0.0F, 0.0F, 1.0F);
                   GlStateManager.translate(0.0F, (entity.ticksExisted + partialTicks) * (0.001F + (float)i * 0.003F) * Chams.cGlintSpeed.getValue(), 0.0F);
                   GlStateManager.matrixMode(GL11.GL_MODELVIEW);
                   if (Chams.cGlintDepth.getValue()) {
                       GL11.glDepthMask(true);
                       GL11.glEnable(GL_DEPTH_TEST);
                   }
                   if (entity.shouldShowBottom()) {
                       this.modelEnderCrystal.render(entity, 0.0f, f3 * 3.0f * Chams.spinSpeed.getValue(), f4 * 0.2f * Chams.bounceSpeed.getValue(), 0.0f, 0.0f, 0.0625f);
                   }
                   else {
                       this.modelEnderCrystalNoBase.render(entity, 0.0f, f3 * 3.0f * Chams.spinSpeed.getValue(), f4 * 0.2f * Chams.bounceSpeed.getValue(), 0.0f, 0.0f, 0.0625f);
                   }
                   if (Chams.cGlintDepth.getValue()) {
                       GL11.glDisable(GL_DEPTH_TEST);
                       GL11.glDepthMask(false);
                   }
               }

               GlStateManager.matrixMode(5890);
               GlStateManager.loadIdentity();
               GlStateManager.matrixMode(5888);

               GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
               GL11.glScalef(1F / Chams.scale.getValue(), 1F / Chams.scale.getValue(), 1F / Chams.scale.getValue());
               GL11.glPopAttrib();
               GL11.glPopMatrix();
           }

       }
    }
}
