package me.darki.konas.gui.container.containers;

import me.darki.konas.gui.container.Container;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.render.ColorUtils;
import me.darki.konas.util.render.RenderUtil;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class InventoryContainer extends Container {

    private Setting<Mode> mode = new Setting<>("Background", Mode.TEXTURE);

    public enum Mode {
        TEXTURE, OUTLINE, OFF
    }

    public InventoryContainer() {
        super("Inventory", 400, 400, 162, 54);
    }

    @Override
    public void onRender() {
        super.onRender();

        if (mode.getValue() == Mode.TEXTURE) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/generic_54.png"));
            mc.ingameGUI.drawTexturedModalRect(getPosX(), getPosY(), 7, 17, 162, 54);
        }

        GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);

        for (int i = 0; i < 3; i++) {
            for (int slot = 0; slot < 9; slot++) {
                int x = (int) (getPosX() + 1 + slot * 18);
                int y = (int) (getPosY() + 1 + i * 18);

                if (mode.getValue() == Mode.OUTLINE) {
                    Gui.drawRect(x - 1, y - 1, x - 1 + 18, y - 1 + 18, new Color(26, 26, 26, 40).hashCode());
                    RenderUtil.drawOutlineRect(x - 1, y - 1, x - 1 + 18, y - 1 + 18, 2f, ColorUtils.rainbow(300, new float[]{1f, 1f, 1f}));
                }

            }
        }
        for (int size = mc.player.inventory.mainInventory.size(), item = 9; item < size; ++item) {
            final int slotX = (int) (getPosX() + 1 + item % 9 * 18);
            final int slotY = (int) (getPosY() + 1 + (item / 9 - 1) * 18);
            GlStateManager.pushMatrix();
            GlStateManager.enableDepth();
            RenderHelper.enableGUIStandardItemLighting();
            mc.getRenderItem().renderItemAndEffectIntoGUI(mc.player.inventory.mainInventory.get(item), slotX, slotY);
            mc.getRenderItem().renderItemOverlays(mc.fontRenderer, mc.player.inventory.mainInventory.get(item), slotX, slotY);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableDepth();
            GlStateManager.popMatrix();
        }

    }

}
