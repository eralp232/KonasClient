package me.darki.konas.module.modules.render;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.Render2DEvent;
import me.darki.konas.module.Module;
import me.darki.konas.util.KonasGlobals;
import me.darki.konas.util.network.APIUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

public class SantaESP extends Module {
    public SantaESP() {
        super("SantaESP", Category.RENDER);
    }

    private ResourceLocation hat = null;

    private static ICamera camera = new Frustum();

    @Override
    public void onEnable() {
        if(hat == null) {
            mc.addScheduledTask(() -> {
                DynamicTexture image = APIUtils.getCapeDynamicTexture("SANTA");
                if(image != null) {
                    ResourceLocation location = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("SANTA_HAT", image);
                    hat = location;
                    Minecraft.getMinecraft().getTextureManager().loadTexture(location, image);
                }
            });
        }
    }

    @Subscriber
    private void onRender2D(Render2DEvent event) {
        if(mc.player == null || mc.world == null || hat == null || mc.getRenderViewEntity() == null) return;

        for (Entity entity : mc.world.loadedEntityList) {
            if (entity == mc.player) continue;
            if (!(entity instanceof EntityPlayer)) continue;
            if (entity.isDead) continue;
            camera.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);
            if(!camera.isBoundingBoxInFrustum(entity.getEntityBoundingBox())) continue;

            Vec3d vec = new Vec3d(entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * mc.getRenderPartialTicks(),
                    entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * mc.getRenderPartialTicks(),
                    entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * mc.getRenderPartialTicks());

            Vec3d topPos = KonasGlobals.INSTANCE.vectorUtils.toScaledScreenPos(vec.add(0, entity.height, 0));
            Vec3d bottomPos = KonasGlobals.INSTANCE.vectorUtils.toScaledScreenPos(vec);

            int height = (int) ((bottomPos.y - topPos.y) * 0.4);
            int width = height;

            int x = (int) (topPos.x - (width / 2));
            int y = (int) topPos.y - (height / 2);

            mc.renderEngine.bindTexture(hat);

            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.enableAlpha();
            Gui.drawScaledCustomSizeModalRect(x, y, 0, 0, width, height, width, height, width, height);
            GlStateManager.disableAlpha();
        }
    }
}
