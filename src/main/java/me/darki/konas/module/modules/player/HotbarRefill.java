package me.darki.konas.module.modules.player;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.UpdateEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.*;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HotbarRefill extends Module {

    private static Setting<Boolean> itemSaver = new Setting<>("ItemSaver", false);
    public Setting<Integer> refillThreshold = new Setting<>("RefillThreshold", 36, 64, 1, 1);
    public Setting<Integer> delay = new Setting<>("Delay", 1, 20, 1, 1);
    private static Setting<Boolean> crystals = new Setting<>("Crystals", true);
    private static Setting<Boolean> xp = new Setting<>("EXp", true);
    private static Setting<Boolean> food = new Setting<>("Food", true);
    private static Setting<Boolean> others = new Setting<>("Others", false);

    public ConcurrentHashMap<ItemStack, Integer> itemsToRefill = new ConcurrentHashMap<>();

    public static me.darki.konas.util.timer.Timer moveTimer = new me.darki.konas.util.timer.Timer();

    private int ticks = 0;

    public HotbarRefill() {
        super("HotbarRefill", "Automatically refills your hotbar", Category.PLAYER);
    }

    @Subscriber
    public void onUpdate(UpdateEvent event) {

        if (mc.player == null || mc.world == null || mc.currentScreen instanceof GuiContainer || event.getPhase() == TickEvent.Phase.START) return;

        if (!moveTimer.hasPassed(350)) return;

        if (itemSaver.getValue()) {
            boolean itemSaved = false;
            EnumHand hands[] = EnumHand.values();
            for (int i = 0; i < hands.length; i++) {
                EnumHand hand = hands[i];
                ItemStack stack = mc.player.getHeldItem(hand);
                if (stack != null && stack.getItem() != null) {
                    Item item = stack.getItem();
                    if (stack.isItemStackDamageable() && stack.getItemDamage() == item.getMaxDamage(stack)) {
                        switch (hand) {
                            case MAIN_HAND: {
                                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, mc.player.inventory.currentItem + 36, 0, ClickType.QUICK_MOVE, mc.player);
                                itemSaved = true;
                                break;
                            }
                            case OFF_HAND: {
                                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 45, 1, ClickType.QUICK_MOVE, mc.player);
                                itemSaved = true;
                                break;
                            }
                        }
                    }
                }
            }
            if (itemSaved) {
                ticks = 0;
                return;
            }
        }
        
        if (ticks > delay.getValue() * 2) {
            if (!mc.player.inventory.getItemStack().isEmpty()) {
                int index = 44;
                while (index >= 9) {
                    if (mc.player.inventoryContainer.getSlot(index).getStack().isEmpty()) {
                        mc.playerController.windowClick(0, index, 0, ClickType.PICKUP, mc.player);
                        return;
                    }
                    index--;
                }
            }
            findItemsToRefill();
            refillItems();
            ticks = 0;
        } else {
            ticks++;
        }



    }

    private void findItemsToRefill() {
        for (int i = 0; i <= 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);

            // If Slot is Empty continue
            if (stack.isEmpty() || stack.getItem() == Items.AIR) continue;

            // If Slot is not stackable continue
            if (!stack.isStackable()) continue;

            // If Slot is full continue
            if (stack.getCount() >= stack.getMaxStackSize()) continue;

            // If Slot is above threshold continue
            if (stack.getCount() >= refillThreshold.getValue()) continue;

            if (others.getValue() || (stack.getItem() instanceof ItemEndCrystal && crystals.getValue()) || (stack.getItem() instanceof ItemFood && food.getValue()) || (stack.getItem() instanceof ItemExpBottle && xp.getValue())) {
                itemsToRefill.put(stack, i);
            }

        }
    }

    private boolean isInventoryGood() {
        for (int i = 0; i < 36; i++) {
            if (!mc.player.inventoryContainer.getSlot(i).getHasStack()) {
                return true;
            }
        }
        return false;
    }

    private void refillItems() {
        for (Map.Entry<ItemStack, Integer> entry : itemsToRefill.entrySet()) {
            ItemStack stack = entry.getKey();
            int slotToRefill = entry.getValue();
            if(mc.player.inventory.getSlotFor(stack) == -1) {
                continue;
            }
            int refillSlot = -1;
            for (int i = 9; i <= 35; i++) {
                ItemStack refillStack = mc.player.inventory.getStackInSlot(i);
                if (refillStack.getItem().equals(stack.getItem())
                        && refillStack.getDisplayName().equals(stack.getDisplayName())
                        && refillStack.getItemDamage() == stack.getItemDamage()) {
                    refillSlot = i;
                    break;
                }
            }
            if(refillSlot != -1) {
                mc.playerController.windowClick(0, refillSlot, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, slotToRefill < 9 ? slotToRefill + 36 : slotToRefill, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, refillSlot, 0, ClickType.PICKUP, mc.player);
                itemsToRefill.remove(stack);
            }
        }
    }

}
