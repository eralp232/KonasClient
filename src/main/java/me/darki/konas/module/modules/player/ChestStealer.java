package me.darki.konas.module.modules.player;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.UpdateEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.timer.Timer;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;

import java.util.Random;

public class ChestStealer extends Module {

    private final Setting<Integer> delay = new Setting<>("Delay", 100, 1000, 1, 1);
    private final Setting<Boolean> random = new Setting<>("Random", false);

    public ChestStealer() {
        super("ChestStealer", "Automatically takes items out of chests", Category.PLAYER, "Looter", "ChestLooter");
    }

    private final Timer timer = new Timer();

    @Subscriber
    public void onUpdate(UpdateEvent event) {
        if (mc.currentScreen instanceof GuiChest) {
            GuiChest chest = (GuiChest) mc.currentScreen;
            ContainerChest mxChest = (ContainerChest) mc.player.openContainer;
            for (int i = 0; i < mxChest.getLowerChestInventory().getSizeInventory(); i++) {
                Slot slot = chest.inventorySlots.getSlot(i);
                if (slot.getHasStack()) {
                    Random random = new Random();
                    if (timer.hasPassed(delay.getValue() + (this.random.getValue() ? random.nextInt(delay.getValue()) : 0))) {
                        mc.playerController.windowClick(mxChest.windowId, i, 0, ClickType.QUICK_MOVE,
                                mc.player);
                        timer.reset();
                    }
                }
            }
            if (isContainerEmpty(mxChest))
                mc.player.closeScreen();
        }
    }


    private boolean isContainerEmpty(Container container) {
        boolean empty = true;
        int i = 0;
        int slotAmount = container.inventorySlots.size() == 90 ? 54 : 27;
        while (i < slotAmount) {
            if (container.getSlot(i).getHasStack()) {
                empty = false;
            }
            ++i;
        }
        return empty;
    }

}
