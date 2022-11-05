package me.darki.konas.gui.kgui.element.setting;

import com.mojang.authlib.GameProfile;
import me.darki.konas.gui.kgui.element.Element;
import me.darki.konas.module.modules.render.Chams;
import me.darki.konas.module.modules.render.ESP;
import me.darki.konas.setting.Preview;
import me.darki.konas.setting.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;

public class PreviewSetting extends Element {
    private final Setting<Preview> setting;

    public PreviewSetting(Setting<Preview> setting, float parentX, float parentY, float offsetX, float offsetY, float width, float height) {
        super(setting.getName(), parentX, parentY, offsetX, offsetY, width, height);
        this.setting = setting;
    }

    @Override
    public void onRender(int mouseX, int mouseY, float partialTicks) {
        super.onRender(mouseX, mouseY, partialTicks);
        EntityOtherPlayerMP fakePlayer = new EntityOtherPlayerMP(Minecraft.getMinecraft().world, new GameProfile(Minecraft.getMinecraft().player.getUniqueID(), Minecraft.getMinecraft().player.getName()));
        fakePlayer.copyLocationAndAnglesFrom(Minecraft.getMinecraft().player);
        fakePlayer.posX = -20000;
        fakePlayer.posZ = -20000;
        fakePlayer.prevRotationPitch = fakePlayer.rotationPitch = -15;
        fakePlayer.prevRotationYaw = fakePlayer.rotationYaw = 20;
        fakePlayer.prevRotationYawHead = fakePlayer.rotationYawHead = 20;
        fakePlayer.ticksExisted = Minecraft.getMinecraft().player.ticksExisted;
        drawPlayerOnScreen((int) (getPosX() + getWidth() / 2), (int) (getPosY() + getHeight() - 10), 100, mouseX, mouseY, fakePlayer, true, true);
    }

    public void drawPlayerOnScreen(int x, int y, int scale, float mouseX, float mouseY, EntityPlayer player, boolean yaw, boolean pitch) {
        Chams.hackyFix = true;
        ESP.hackyFix = true;
        GlStateManager.pushMatrix();
        GlStateManager.enableDepth();
        GlStateManager.color(1f, 1f, 1f);
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, 50.0F);
        GlStateManager.scale((float) (-scale), (float) scale, (float) scale);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        float f = player.renderYawOffset;
        float f1 = player.rotationYaw;
        float f2 = player.rotationPitch;
        float f3 = player.prevRotationYawHead;
        float f4 = player.rotationYawHead;
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
        mouseX = yaw ? player.rotationYaw * -1 : mouseX;
        mouseY = pitch ? player.rotationPitch * -1 : mouseY;
        GlStateManager.rotate(-((float) Math.atan(mouseY / 40.0F)) * 20.0F, 1.0F, 0.0F, 0.0F);
        if (!yaw) {
            player.renderYawOffset = (float) Math.atan(mouseX / 40.0F) * 20.0F;
            player.rotationYaw = (float) Math.atan(mouseX / 40.0F) * 40.0F;
            player.rotationYawHead = player.rotationYaw;
            player.prevRotationYawHead = player.rotationYaw;
        }
        if (!pitch) {
            player.rotationPitch = -((float) Math.atan(mouseY / 40.0F)) * 20.0F;
        }
        GlStateManager.translate(0.0F, 0.0F, 0.0F);
        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        rendermanager.setPlayerViewY(180.0F);
        rendermanager.setRenderShadow(false);
        rendermanager.renderEntity(player, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
        rendermanager.setRenderShadow(true);
        if (!yaw) {
            player.renderYawOffset = f;
            player.rotationYaw = f1;
            player.prevRotationYawHead = f3;
            player.rotationYawHead = f4;
        }
        if (!pitch) {
            player.rotationPitch = f2;
        }
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.disableDepth();
        GlStateManager.popMatrix();
        ESP.hackyFix = false;
        Chams.hackyFix = false;
    }
}
