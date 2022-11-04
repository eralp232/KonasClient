package me.darki.konas.gui.beacon;

import me.darki.konas.module.modules.exploit.BeaconSelector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiBeacon;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.IInventory;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Made by Darki with help from TBM
 */

public class CustomGuiBeacon extends GuiBeacon {

    private static final ResourceLocation BEACON_GUI_TEXTURES = new ResourceLocation("textures/gui/container/beacon.png");
    public static final Potion[][] EFFECTS_LIST = new Potion[][]{{MobEffects.SPEED, MobEffects.HASTE}, {MobEffects.RESISTANCE, MobEffects.JUMP_BOOST}, {MobEffects.STRENGTH}, {MobEffects.REGENERATION}};

    private boolean renderButtons;

    public CustomGuiBeacon(InventoryPlayer playerInventory, IInventory tileBeaconIn) {
        super(playerInventory, tileBeaconIn);
    }

    @Override
    public void initGui() {
        super.initGui();
        renderButtons = true;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (renderButtons) {
            int id = 20;
            int offsetY = this.guiTop;
            for (Potion[] potions : EFFECTS_LIST) {
                for (Potion potion : potions) {
                    CustomGuiBeacon.PowerButton custompotion =
                            new CustomGuiBeacon.PowerButton(id, guiLeft - 27, offsetY, potion, 0);
                    this.buttonList.add(custompotion);
                    if (potion == Potion.getPotionById(BeaconSelector.effect)) {
                        custompotion.setSelected(true);
                    }
                    offsetY += 27;
                    id++;
                }
            }
        }
    }

    @Override
    protected void actionPerformed(@NotNull GuiButton button) throws IOException {
        super.actionPerformed(button);

        if (button instanceof CustomGuiBeacon.PowerButton) {
            CustomGuiBeacon.PowerButton guibeacon$powerbutton = (CustomGuiBeacon.PowerButton) button;

            if (guibeacon$powerbutton.isSelected()) {
                return;
            }

            int potionId = Potion.getIdFromPotion(guibeacon$powerbutton.effect);

            if (guibeacon$powerbutton.tier < 3) {
                BeaconSelector.effect = potionId;
            }

            this.buttonList.clear();
            this.initGui();
            this.updateScreen();

        }

    }

    @SideOnly(Side.CLIENT)
    class PowerButton extends CustomButton {
        private final Potion effect;
        private final int tier;

        public PowerButton(int buttonId, int x, int y, Potion effectIn, int tierIn) {
            super(buttonId, x, y, GuiContainer.INVENTORY_BACKGROUND, effectIn.getStatusIconIndex() % 8 * 18, 198 + effectIn.getStatusIconIndex() / 8 * 18);
            this.effect = effectIn;
            this.tier = tierIn;
        }

        public void drawButtonForegroundLayer(int mouseX, int mouseY) {
            String s = I18n.format(this.effect.getName());

            if (this.tier >= 3 && this.effect != MobEffects.REGENERATION) {
                s = s + " II";
            }

            CustomGuiBeacon.this.drawHoveringText(s, mouseX, mouseY);
        }
    }

    @SideOnly(Side.CLIENT)
    static class CustomButton extends GuiButton {
        private final ResourceLocation iconTexture;
        private final int iconX;
        private final int iconY;
        private boolean selected;

        protected CustomButton(int buttonId, int x, int y, ResourceLocation iconTextureIn, int iconXIn, int iconYIn) {
            super(buttonId, x, y, 22, 22, "");
            this.iconTexture = iconTextureIn;
            this.iconX = iconXIn;
            this.iconY = iconYIn;
        }

        /**
         * Draws this button to the screen.
         */
        public void drawButton(@NotNull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
            if (this.visible) {
                mc.getTextureManager().bindTexture(BEACON_GUI_TEXTURES);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
                int j = 0;

                if (!this.enabled) {
                    j += this.width * 2;
                } else if (this.selected) {
                    j += this.width;
                } else if (this.hovered) {
                    j += this.width * 3;
                }

                this.drawTexturedModalRect(this.x, this.y, j, 219, this.width, this.height);

                if (!BEACON_GUI_TEXTURES.equals(this.iconTexture)) {
                    mc.getTextureManager().bindTexture(this.iconTexture);
                }

                this.drawTexturedModalRect(this.x + 2, this.y + 2, this.iconX, this.iconY, 18, 18);
            }
        }

        public boolean isSelected() {
            return this.selected;
        }

        public void setSelected(boolean selectedIn) {
            this.selected = selectedIn;
        }
    }

}
