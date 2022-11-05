package me.darki.konas.module.modules.client;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.DrawGuiScreenEvent;
import me.darki.konas.event.events.RenderItemToolTipEvent;
import me.darki.konas.event.events.RenderTooltipBackgroundEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.ColorSetting;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.render.font.FontRendererWrapper;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemAir;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import java.awt.*;

public class Tooltips extends Module {
    private static Setting<Boolean> texture = new Setting<>("Texture", true);
    private static Setting<Boolean> customColor = new Setting<>("CustomColor", false);
    private static Setting<ColorSetting> customColorValue = new Setting<>("Color", new ColorSetting(0xFFFF00FF, true));
    private static Setting<Boolean> shulkers = new Setting<>("Shulkers", true);
    private static Setting<Boolean> maps = new Setting<>("Maps", true);

    private int mapX;
    private int mapY;

    public Tooltips() {
        super("Tooltips", "Enchances inventory tooltips", Category.CLIENT);
    }

    @Subscriber
    public void onPostBackgroundTooltipRender(RenderTooltipBackgroundEvent event) {
        if (maps.getValue() && event.getItemStack().getItem() instanceof ItemMap) {
            mapX = event.getX();
            mapY = event.getY();
        }
    }

    @Subscriber
    public void onItemTooltip(ItemTooltipEvent event) {
        if (maps.getValue() && event.getItemStack().getItem() instanceof ItemMap) {
            event.getToolTip().clear();
            event.getToolTip().add(event.getItemStack().getDisplayName());
        }
    }

    @Subscriber
    public void onDrawGuiScreen(DrawGuiScreenEvent event) {
        if (maps.getValue() && event.getGui() instanceof GuiContainer) {
            if (mc.player.inventory.getItemStack().getItem() instanceof ItemAir) {
                Slot slotUnderMouse = ((GuiContainer) event.getGui()).getSlotUnderMouse();
                if (slotUnderMouse == null || !slotUnderMouse.getHasStack()) {
                    return;
                }
                ItemStack itemUnderMouse = slotUnderMouse.getStack();
                if (itemUnderMouse.getItem() instanceof ItemMap) {
                    MapData mapdata = ((ItemMap) itemUnderMouse.getItem()).getMapData(itemUnderMouse, mc.world);
                    if (mapdata == null) {
                        return;
                    }
                    GlStateManager.disableDepth();
                    GlStateManager.disableLighting();
                    mc.getTextureManager().bindTexture(new ResourceLocation("textures/map/map_background.png"));
                    Tessellator tessellator = Tessellator.getInstance();
                    BufferBuilder bufferbuilder = tessellator.getBuffer();
                    GlStateManager.translate(mapX, mapY - 72D, 0D);
                    GlStateManager.scale(0.5D, 0.5D, 1.0D);
                    bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
                    bufferbuilder.pos(-7.0D, 135.0D, 0.0D).tex(0.0D, 1.0D).endVertex();
                    bufferbuilder.pos(135.0D, 135.0D, 0.0D).tex(1.0D, 1.0D).endVertex();
                    bufferbuilder.pos(135.0D, -7.0D, 0.0D).tex(1.0D, 0.0D).endVertex();
                    bufferbuilder.pos(-7.0D, -7.0D, 0.0D).tex(0.0D, 0.0D).endVertex();
                    tessellator.draw();
                    mc.entityRenderer.getMapItemRenderer().renderMap(mapdata, true);
                    GlStateManager.enableLighting();
                    GlStateManager.enableDepth();
                }
            }
        }
    }

    @Subscriber
    public void onRenderToolTip(RenderItemToolTipEvent event) {
        if (shulkers.getValue() && event.getItemStack().getItem() instanceof ItemShulkerBox) {
            this.renderShulkerTip(event.getItemStack(), event.getX(), event.getY());
            event.setCancelled(true);
        }
    }

    private void renderShulkerTip(ItemStack shulkerStack, int x, int y) {
        final NBTTagCompound tagCompound = shulkerStack.getTagCompound();

        GlStateManager.enableBlend();
        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();

        if (texture.getValue()) {
            // Width we need for our box
            int width = 144; //9*16

            int offsetX = x + 12;
            int offsetT = y - 12;
            int height = 48 + FontRendererWrapper.getFontHeight(); //3*16

            mc.getRenderItem().zLevel = 300.0F;

            // That last bit looks awful, but basically it gets the color!
            final Color color = customColor.getValue() ? new Color(customColorValue.getValue().getColor()) : new Color(((BlockShulkerBox) ((ItemShulkerBox) shulkerStack.getItem()).getBlock()).getColor().getColorValue());

            drawTexturedModalRect(offsetX - 8.5, offsetT - 3, 0, 0, width + 3, height + 6, color);

            // add 2 more pixels to texture, don't foget to scale y stuff
            mc.fontRenderer.drawString(shulkerStack.getDisplayName(), x + 8, y - 12, 0xffffff);

            GlStateManager.enableBlend();
            GlStateManager.enableAlpha();
            GlStateManager.enableTexture2D();
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
            RenderHelper.enableGUIStandardItemLighting();

            // This makes ShulkerPreview more compact!
            GlStateManager.scale(0.75, 0.75, 0.75);
            if (tagCompound != null) {
                NBTTagCompound blockEntityTag = tagCompound.getCompoundTag("BlockEntityTag");
                if (!blockEntityTag.isEmpty() && blockEntityTag.getTagList("Items", 10) != null) {

                    NonNullList<ItemStack> nonnulllist = NonNullList.withSize(27, ItemStack.EMPTY);
                    ItemStackHelper.loadAllItems(blockEntityTag, nonnulllist);

                    for (int i = 0; i < nonnulllist.size(); i++) {
                        int iX = x + (i % 9) * 15 + 11;
                        int iY = y + (i / 9) * 15 - 11 + 10;
                        iX /= 0.75;
                        iY /= 0.75;
                        ItemStack itemStack = nonnulllist.get(i);

                        mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, iX, iY);
                        mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, iX, iY, null);
                    }
                }
            }
            GlStateManager.scale(-0.75, -0.75, -0.75);
            RenderHelper.disableStandardItemLighting();
            mc.getRenderItem().zLevel = 0.0F;
        } else {
            // Width we need for our box
            float width = Math.max(144, FontRendererWrapper.getStringWidth(shulkerStack.getDisplayName()) + 3); //9*16


            int offsetX = x + 12;
            int offsetT = y - 12;
            int height = 48 + FontRendererWrapper.getFontHeight(); //3*16

            mc.getRenderItem().zLevel = 300.0F;

            // That last bit looks awful, but basically it gets the color!
            final Color color = customColor.getValue() ? new Color(customColorValue.getValue().getColor()) : new Color(((BlockShulkerBox) ((ItemShulkerBox) shulkerStack.getItem()).getBlock()).getColor().getColorValue());

            final Color modifiedColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 150);

            GuiScreen.drawRect(offsetX - 3, offsetT - 3, (int) (offsetX + width + 3), offsetT + height + 3, modifiedColor.getRGB());

            try {
                FontRendererWrapper.drawString(shulkerStack.getDisplayName(), x + 12, y - 12, 0xFFFFFFFF);
            } catch (NullPointerException exception) {
                System.out.println("Error rendering font");
            }

            GlStateManager.enableBlend();
            GlStateManager.enableAlpha();
            GlStateManager.enableTexture2D();
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
            RenderHelper.enableGUIStandardItemLighting();
            if (tagCompound != null) {
                NBTTagCompound blockEntityTag = tagCompound.getCompoundTag("BlockEntityTag");

                NonNullList<ItemStack> nonnulllist = NonNullList.withSize(27, ItemStack.EMPTY);
                ItemStackHelper.loadAllItems(blockEntityTag, nonnulllist);

                for (int i = 0; i < nonnulllist.size(); i++) {
                    int iX = x + (i % 9) * 16 + 11;
                    int iY = y + (i / 9) * 16 - 11 + 8;
                    ItemStack itemStack = nonnulllist.get(i);

                    mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, iX, iY);
                    mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, iX, iY, null);
                }
            }
            RenderHelper.disableStandardItemLighting();
            mc.getRenderItem().zLevel = 0.0F;
        }

        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableRescaleNormal();
    }

    public void drawTexturedModalRect(double x, double y, double textureX, double textureY, double width, double height, Color color) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        mc.getTextureManager().bindTexture(new ResourceLocation("konas/textures/container.png"));

        // Inline values (scale, because minecraft texture rendering is weird)
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        bufferbuilder.pos((x), (y + height), 69).tex(((float)(textureX) * 0.00683593F), ((float)(textureY + height) * 0.015676616F)).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();
        bufferbuilder.pos((x + width), (y + height), 69).tex(((float)(textureX + width) * 0.0068F), ((float)(textureY + height) * 0.015676616F)).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();
        bufferbuilder.pos((x + width), (y + 0), 69).tex(((float)(textureX + width) * 0.0068F), ((float)(textureY) * 0.015676616F)).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();
        bufferbuilder.pos((x), (y + 0), 69).tex(((float)(textureX) * 0.00683593F), ((float)(textureY) * 0.015676616F)).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();
        tessellator.draw();
    }
}
