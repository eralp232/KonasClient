package me.darki.konas.module.modules.combat;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.RootEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.timer.Timer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.util.EnumHand;

public class StrictTotem extends Module {
    private static Setting<Integer> delay = new Setting<>("Delay", 0, 20, 0, 1);
    private static Setting<Boolean> cancelMotion = new Setting<>("CancelMotion", false);

    public StrictTotem() {
        super("StrictTotem", "Forces totem into offhand", Category.COMBAT);
        setProtocolRange(315, 1000);
    }

    private Timer timer = new Timer();

    private boolean hasTotem = false;

    @Subscriber
    public void onRoot(RootEvent event) {
        if (!hasTotem) {
            timer.reset();
        }

        if (mc.player == null || mc.world == null) return;

        if (!(mc.currentScreen instanceof GuiContainer && !(mc.currentScreen instanceof GuiInventory) || mc.player.getHeldItem(EnumHand.OFF_HAND).getItem() == Items.TOTEM_OF_UNDYING || mc.player.isCreative())) {
            int index = 44;
            while (index >= 9) {
                if (mc.player.inventoryContainer.getSlot(index).getStack().getItem() == Items.TOTEM_OF_UNDYING) {
                    hasTotem = true;

                    if (timer.hasPassed(delay.getValue() * 100F) && mc.player.inventory.getItemStack().getItem() != Items.TOTEM_OF_UNDYING) {
                        if (cancelMotion.getValue() && mc.player.motionX * mc.player.motionX + mc.player.motionY * mc.player.motionY + mc.player.motionZ * mc.player.motionZ >= 9.0E-4D) {
                            mc.player.motionX = 0D;
                            mc.player.motionY = 0D;
                            mc.player.motionZ = 0D;
                            return;
                        }
                        mc.playerController.windowClick(0, index, 0, ClickType.PICKUP, mc.player);
                    }

                    if (timer.hasPassed(delay.getValue() * 200F) && mc.player.inventory.getItemStack().getItem() == Items.TOTEM_OF_UNDYING) {
                        if (cancelMotion.getValue() && mc.player.motionX * mc.player.motionX + mc.player.motionY * mc.player.motionY + mc.player.motionZ * mc.player.motionZ >= 9.0E-4D) {
                            mc.player.motionX = 0D;
                            mc.player.motionY = 0D;
                            mc.player.motionZ = 0D;
                            return;
                        }
                        mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
                        if (mc.player.inventory.getItemStack().isEmpty()) {
                            hasTotem = false;
                            return;
                        }
                    }

                    if (timer.hasPassed(delay.getValue() * 300F) && !mc.player.inventory.getItemStack().isEmpty() && mc.player.getHeldItem(EnumHand.OFF_HAND).getItem() == Items.TOTEM_OF_UNDYING) {
                        if (cancelMotion.getValue() && mc.player.motionX * mc.player.motionX + mc.player.motionY * mc.player.motionY + mc.player.motionZ * mc.player.motionZ >= 9.0E-4D) {
                            mc.player.motionX = 0D;
                            mc.player.motionY = 0D;
                            mc.player.motionZ = 0D;
                            return;
                        }
                        mc.playerController.windowClick(0, index, 0, ClickType.PICKUP, mc.player);
                        hasTotem = false;
                        return;
                    }
                }
                index--;
            }
        }
    }

    public void onEnable() {
        hasTotem = false;
    }
}
