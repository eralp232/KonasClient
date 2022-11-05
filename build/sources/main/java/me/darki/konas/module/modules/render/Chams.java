package me.darki.konas.module.modules.render;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.RenderArmEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.ColorSetting;
import me.darki.konas.setting.Parent;
import me.darki.konas.setting.Preview;
import me.darki.konas.setting.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.*;

public class Chams extends Module {

    public Chams() {
        super("Chams", "Makes you see entities through walls", Category.RENDER);
    }

    public static Setting<Parent> living = new Setting<>("Living", new Parent(false));
    public static Setting<Preview> preview = new Setting<>("Preview", new Preview(false, true)).withParent(living);
    public static Setting<Boolean> lRender = new Setting<>("Render", false).withParent(living);
    public static Setting<Boolean> lRenderDepth = new Setting<>("RenderDepth", false).withParent(living);
    public static Setting<GlintMode> lGlintMode = new Setting<>("Glint", GlintMode.NONE).withParent(living);
    public static Setting<Boolean> lGlintDepth = new Setting<>("GlintDepth", false).withParent(living);
    public static Setting<Float> lGlintSpeed = new Setting<>("GlintSpeed", 5F, 20F, 0.1F, 0.1F).withParent(living);
    public static Setting<Float> lGlintScale = new Setting<>("GlintScale", 1F, 10F, 0.1F, 0.1F).withParent(living);
    public static Setting<ColorSetting> lGlintColor = new Setting<>("GlintColor", new ColorSetting(0x7700FFFF)).withParent(living);
    public static Setting<Boolean> lFill = new Setting<>("Fill", false).withParent(living);
    public static Setting<Boolean> lFillDepth = new Setting<>("FillDepth", false).withParent(living);
    public static Setting<Boolean> lLighting = new Setting<>("Lighting", false).withParent(living);
    public static Setting<ColorSetting> lFillColor = new Setting<>("FillColor", new ColorSetting(0x7700FFFF)).withParent(living);
    public static Setting<Boolean> lOutline = new Setting<>("Outline", false).withParent(living);
    public static Setting<Boolean> lOutlineDepth = new Setting<>("OutlineDepth", false).withParent(living);
    public static Setting<Float> lWidth = new Setting<>("Width", 1.0F, 10F, 0.1F, 0.1F).withParent(living);
    public static Setting<ColorSetting> lOutlineColor = new Setting<>("GOutlineColor", new ColorSetting(0xFF00FFFF)).withParent(living);
    public static Setting<Boolean> players = new Setting<>("Players", true).withParent(living);
    public static Setting<Boolean> self = new Setting<>("Self", true).withParent(living);
    public static Setting<Boolean> animals = new Setting<>("Animals", true).withParent(living);
    public static Setting<Boolean> monsters = new Setting<>("Monsters", true).withParent(living);
    public static Setting<Boolean> invisibles = new Setting<>("Invis", true).withParent(living);

    public static Setting<Parent> crystal = new Setting<>("Crystal", new Parent(false));
    public static Setting<Boolean> crystalSetting = new Setting<>("Crystals", true).withParent(crystal);
    public static Setting<Boolean> cRender = new Setting<>("CRender", false).withParent(crystal);
    public static Setting<Boolean> cRenderDepth = new Setting<>("CRenderDepth", false).withParent(crystal);
    public static Setting<GlintMode> cGlintMode = new Setting<>("CGlint", GlintMode.NONE).withParent(crystal);
    public static Setting<Boolean> cGlintDepth = new Setting<>("CGlintDepth", false).withParent(crystal);
    public static Setting<Float> cGlintSpeed = new Setting<>("CGlintSpeed", 5F, 20F, 0.1F, 0.1F).withParent(crystal);
    public static Setting<Float> cGlintScale = new Setting<>("CGlintScale", 1F, 10F, 0.1F, 0.1F).withParent(crystal);
    public static Setting<ColorSetting> cGlintColor = new Setting<>("CGlintColor", new ColorSetting(0x770000FF)).withParent(crystal);
    public static Setting<Boolean> cFill = new Setting<>("CFill", false).withParent(crystal);
    public static Setting<Boolean> cFillDepth = new Setting<>("CFillDepth", false).withParent(crystal);
    public static Setting<Boolean> cLighting = new Setting<>("CLighting", false).withParent(crystal);
    public static Setting<ColorSetting> cFillColor = new Setting<>("CFillColor", new ColorSetting(0x770000FF)).withParent(crystal);
    public static Setting<Boolean> cOutline = new Setting<>("COutline", false).withParent(crystal);
    public static Setting<Boolean> cOutlineDepth = new Setting<>("COutlineDepth", false).withParent(crystal);
    public static Setting<Float> cWidth = new Setting<>("CWidth", 1.0F, 10F, 0.1F, 0.1F).withParent(crystal);
    public static Setting<ColorSetting> cOutlineColor = new Setting<>("CGOutlineColor", new ColorSetting(0xFF0000FF)).withParent(crystal);
    public static Setting<Float> scale = new Setting<>("Scale", 1.0F, 10F, 0.1F, 0.1F).withParent(crystal);
    public static Setting<Float> spinSpeed = new Setting<>("SpinSpeed", 1.0F, 10F, 0.1F, 0.1F).withParent(crystal);
    public static Setting<Float> bounceSpeed = new Setting<>("Bounciness", 1.0F, 10F, 0.1F, 0.1F).withParent(crystal);

    public static Setting<Parent> hand = new Setting<>("Hand", new Parent(false));
    public static Setting<Boolean> handSetting = new Setting<>("Hands", true).withParent(hand);
    public static Setting<ColorSetting> handColor = new Setting<>("HandColor", new ColorSetting(0x1C0000FF)).withVisibility(handSetting::getValue).withParent(hand);
    public static Setting<GlintMode> hGlintMode = new Setting<>("HGlint", GlintMode.NONE).withParent(hand);
    public static Setting<Float> hGlintSpeed = new Setting<>("HGlintSpeed", 5F, 20F, 0.1F, 0.1F).withParent(hand);
    public static Setting<Float> hGlintScale = new Setting<>("HGlintScale", 1F, 10F, 0.1F, 0.1F).withParent(hand);
    public static Setting<ColorSetting> hGlintColor = new Setting<>("HGlintColor", new ColorSetting(0x770000FF)).withParent(hand);

    public static final ResourceLocation ENCHANTED_ITEM_GLINT_RES = new ResourceLocation("textures/misc/enchanted_item_glint.png");
    public static final ResourceLocation LIGHTNING_TEXTURE = new ResourceLocation("konas/textures/enchant_glint.png");

    public enum GlintMode {
        NONE, VANILLA, CUSTOM
    }

    public static boolean hackyFix = false;

    public static boolean onRenderEntity(ModelBase modelBase, Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (!shouldRender(entityIn)) {
            return false;
        }

        boolean fancyGraphics = mc.gameSettings.fancyGraphics;
        mc.gameSettings.fancyGraphics = false;

        if(lRender.getValue()) {
            if (!Chams.lRenderDepth.getValue()) {
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
                glEnable(GL11.GL_POLYGON_OFFSET_FILL);
                glPolygonOffset(1.0f, -1100000.0f);
            }
            modelBase.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            if (!Chams.lRenderDepth.getValue()) {
                glDisable(GL11.GL_POLYGON_OFFSET_FILL);
                glPolygonOffset(1.0f, 1100000.0f);
            }
        }

        float gamma = mc.gameSettings.gammaSetting;
        mc.gameSettings.gammaSetting = 10000.0F;

        if (lFill.getValue()) {
            renderFill(lFillColor.getValue(), modelBase, entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, 0.0f);
        }

        if (lOutline.getValue()) {
            renderWireFrame(lOutlineColor.getValue(), lWidth.getValue(), modelBase, entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, 0.0f);
        }

        if (lGlintMode.getValue() != GlintMode.NONE) {
            renderGlint(lGlintColor.getValue(), modelBase, entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, 0.0f);
        }

        try {
            mc.gameSettings.fancyGraphics = fancyGraphics;
            mc.gameSettings.gammaSetting = gamma;
        } catch (Exception exception) {}

        return true;
    }

    public static void renderGlint(ColorSetting colorSetting, ModelBase modelBase, Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, float offset) {
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);

        Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(lGlintMode.getValue() == GlintMode.CUSTOM ? Chams.LIGHTNING_TEXTURE : Chams.ENCHANTED_ITEM_GLINT_RES);
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL_FILL);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glColor4f(colorSetting.getRed() / 255F, colorSetting.getGreen() / 255F, colorSetting.getBlue() / 255F, colorSetting.getAlpha() / 255F);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);

        for (int i = 0; i < 2; ++i)
        {
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.loadIdentity();
            float tScale = 0.33333334F * lGlintScale.getValue();
            GlStateManager.scale(tScale, tScale, tScale);
            GlStateManager.rotate(30.0F - (float)i * 60.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.translate(0.0F, (entityIn.ticksExisted + mc.getRenderPartialTicks()) * (0.001F + (float)i * 0.003F) * Chams.lGlintSpeed.getValue(), 0.0F);
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            GL11.glTranslatef(offset, offset, offset);
            GlStateManager.color(colorSetting.getRed() / 255f, colorSetting.getGreen() / 255f, colorSetting.getBlue() / 255f, colorSetting.getAlpha() / 255f);
            if (Chams.lGlintDepth.getValue()) {
                GL11.glDepthMask(true);
                GL11.glEnable(GL_DEPTH_TEST);
            }
            modelBase.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            if (Chams.lGlintDepth.getValue()) {
                GL11.glDisable(GL_DEPTH_TEST);
                GL11.glDepthMask(false);
            }
        }

        GlStateManager.matrixMode(5890);
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(5888);

        GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        GlStateManager.color(1F, 1F, 1F, 1F);
        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

    public static void renderWireFrame(ColorSetting colorSetting, float width, ModelBase modelBase, Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, float offset) {
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glTranslatef(offset, offset, offset);
        GlStateManager.color(colorSetting.getRed() / 255f, colorSetting.getGreen() / 255f, colorSetting.getBlue() / 255f, colorSetting.getAlpha() / 255f);
        GlStateManager.glLineWidth(width);
        if (Chams.lOutlineDepth.getValue()) {
            GL11.glDepthMask(true);
            GL11.glEnable(GL_DEPTH_TEST);
        }
        modelBase.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        if (Chams.lOutlineDepth.getValue()) {
            GL11.glDisable(GL_DEPTH_TEST);
            GL11.glDepthMask(false);
        }
        GlStateManager.resetColor();
        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

    public static void renderFill(ColorSetting colorSetting, ModelBase modelBase, Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, float offset) {
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        if (lLighting.getValue()) {
            GL11.glEnable(GL11.GL_LIGHTING);
        } else {
            GL11.glDisable(GL11.GL_LIGHTING);
        }
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glTranslatef(offset, offset, offset);
        GlStateManager.color(colorSetting.getRed() / 255f, colorSetting.getGreen() / 255f, colorSetting.getBlue() / 255f, colorSetting.getAlpha() / 255f);
        if (Chams.lFillDepth.getValue()) {
            GL11.glDepthMask(true);
            GL11.glEnable(GL_DEPTH_TEST);
        }
        modelBase.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        if (Chams.lFillDepth.getValue()) {
            GL11.glDisable(GL_DEPTH_TEST);
            GL11.glDepthMask(false);
        }
        GlStateManager.resetColor();
        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

    @Subscriber
    public void onRenderArm(RenderArmEvent event) {
        if (handSetting.getValue()) {
            event.setRed(handColor.getValue().getRed());
            event.setGreen(handColor.getValue().getGreen());
            event.setBlue(handColor.getValue().getBlue());
            event.setAlpha(handColor.getValue().getAlpha());
            event.setCancelled(true);
        }
    }

    private static boolean shouldRender(Entity entity) {
        if(entity == null) {
            return false;
        }
        if(entity.isInvisible() && !invisibles.getValue()) {
            return false;
        }
        if(entity.equals(mc.player) && !self.getValue()) {
            return false;
        }
        if(entity instanceof EntityPlayer && !players.getValue()) {
            return false;
        }
        if(isPassiveEntity(entity) && !animals.getValue()) {
            return false;
        }
        if(!isPassiveEntity(entity) && !(entity instanceof EntityPlayer) && !monsters.getValue()) {
            return false;
        }
        return true;
    }

    public static boolean isPassiveEntity(Entity entity) {
        if (entity instanceof EntityWolf) {
            return !((EntityWolf)entity).isAngry();
        }
        if (entity instanceof EntityAgeable || entity instanceof EntityTameable || entity instanceof EntityAmbientCreature || entity instanceof EntitySquid) {
            return true;
        }
        if (entity instanceof EntityIronGolem) {
            return (((EntityIronGolem)entity).getRevengeTarget() == null);
        }
        return false;
    }


}
