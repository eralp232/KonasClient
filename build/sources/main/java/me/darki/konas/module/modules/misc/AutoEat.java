package me.darki.konas.module.modules.misc;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.UpdateEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.util.EnumHand;

public class AutoEat extends Module {

    private final Setting<Float> health = new Setting<>("Health", 10f, 36f, 0f, 1f);
    private final Setting<Float> hunger = new Setting<>("Hunger", 15f, 20f, 0f, 1f);
    private final Setting<Boolean> autoSwitch = new Setting<>("AutoSwitch", true);
    private final Setting<Boolean> preferGaps = new Setting<>("PreferGaps", false);

    public AutoEat() {
        super("AutoEat", Category.MISC, "AutoFood");
    }

    int originalSlot = -1;
    boolean firstSwap = true;
    boolean resetKeyBind = false;

    @Subscriber
    public void onUpdate(UpdateEvent event) {
        if (mc.player == null || mc.world == null) return;

        if(mc.player.isCreative()) return;

        if (mc.player.getHealth() + mc.player.getAbsorptionAmount() <= health.getValue()
                || mc.player.getFoodStats().getFoodLevel() <= hunger.getValue()) {

            if (autoSwitch.getValue()) {

                int foodSlot = findFoodSlot();
                if (firstSwap) {
                    originalSlot = mc.player.inventory.currentItem;
                    firstSwap = false;
                }

                if (foodSlot != -1) {
                    mc.player.inventory.currentItem = foodSlot;
                    mc.player.connection.sendPacket(new CPacketHeldItemChange(foodSlot));
                }

            }

            if (mc.player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemFood) {
                if (mc.currentScreen == null || mc.currentScreen instanceof GuiInventory) {
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), true);
                    resetKeyBind = true;
                } else {
                    mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);
                }
            } else if (mc.currentScreen == null || mc.currentScreen instanceof GuiInventory) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), GameSettings.isKeyDown(mc.gameSettings.keyBindUseItem));
            }

        } else {

            if(resetKeyBind) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), GameSettings.isKeyDown(mc.gameSettings.keyBindUseItem));
                resetKeyBind = false;
            }

            if (originalSlot != -1) {
                mc.player.inventory.currentItem = originalSlot;
                mc.player.connection.sendPacket(new CPacketHeldItemChange(originalSlot));
                originalSlot = -1;
                firstSwap = true;
            }
        }

    }

    private int findFoodSlot() {

        int foodSlot = -1;
        float bestHealAmount = 0F;

        if (foodSlot == -1) {
            for (int l = 0; l < 9; ++l) {
                ItemStack item = mc.player.inventory.getStackInSlot(l);

                if (item.getItem() instanceof ItemFood) {

                    if(preferGaps.getValue() && item.getItem() == Items.GOLDEN_APPLE) {
                        foodSlot = l;
                        break;
                    }

                    float healAmount = ((ItemFood) item.getItem()).getHealAmount(item);

                    if(healAmount > bestHealAmount) {
                        bestHealAmount = healAmount;
                        foodSlot = l;
                    }

                }
            }
        }

        return foodSlot;

    }

}
